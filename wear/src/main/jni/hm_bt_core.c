/*
 * hm_bt_core.c
 *
 *  Created on: Oct 21, 2014
 *      Author: Maidu �le
 */

#include "hm_bt_core.h"
#include "hm_config.h"
#include "hm_conf_access.h"
#include "hm_bt_api.h"
#include "hm_ctw_customer.h"
#include "hm_cert.h"
#include "hm_bt_hal.h"
#include "hm_bt_crypto_hal.h"
#include "hm_bt_debug_hal.h"
#include "hm_bt_persistence_hal.h"

static uint8_t BLE_ON = 1;

#define MAX_CLIENTS  5

#define MAX_COMMAND_SIZE 220
#define PACKET_BEGIN    0x00
#define PACKET_END      0xFF
#define PACKET_ESCAPE   0xFE

static uint32_t clock = 0;
static bool done = false;
static bool isAuth = false;

#define APPL_LOG                         app_trace_log                                  /**< Debug logger macro that will be used in this file to do logging of debug information over UART. */

#define MIN_CONNECTION_INTERVAL    MSEC_TO_UNITS(7.5, UNIT_1_25_MS)   /**< Determines maximum connection interval in millisecond. */
#define MAX_CONNECTION_INTERVAL    MSEC_TO_UNITS(10, UNIT_1_25_MS)    /**< Determines maximum connection interval in millisecond. */
#define SLAVE_LATENCY              0                                  /**< Determines slave latency in counts of connection events. */
#define SUPERVISION_TIMEOUT        MSEC_TO_UNITS(4000, UNIT_10_MS)    /**< Determines supervision time-out in units of 10 millisecond. */

#define TARGET_UUID                0x713D                             /**< Target device name that application is looking for. */
#define MAX_PEER_COUNT             DEVICE_MANAGER_MAX_CONNECTIONS     /**< Maximum number of peer's application intends to manage. */
#define UUID16_SIZE                2                                  /**< Size of 16 bit UUID */

/**@breif Macro to unpack 16bit unsigned UUID from octet stream. */
#define UUID16_EXTRACT(DST,SRC)                                                                  \
        do                                                                                       \
        {                                                                                        \
            (*(DST)) = (SRC)[1];                                                                 \
            (*(DST)) <<= 8;                                                                      \
            (*(DST)) |= (SRC)[0];                                                                \
        } while(0)

#define NRF_SUCCESS 0x0

/**@brief Variable length data encapsulation in terms of length and pointer to data */
typedef struct
{
    uint8_t     * p_data;                                         /**< Pointer to data. */
    uint16_t      data_len;                                       /**< Length of data. */
}data_t;

typedef enum
{
    BLE_NO_SCAN,                                                  /**< No advertising running. */
    BLE_WHITELIST_SCAN,                                           /**< Advertising with whitelist. */
    BLE_FAST_SCAN,                                                /**< Fast advertising running. */
} ble_advertising_mode_t;

/**@brief Client states. */
typedef enum
{
    IDLE,
    STATE_SERVICE_DISC,
    STATE_NOTIF_ENABLE,
    STATE_SERIAL_GETTING,
    STATE_RUNNING,
    STATE_ERROR
} client_state_t;

typedef struct
{
	bool isEmpty;
	bool isLeaved;
	int8_t rssi;
  uint8_t is_entered_reported;
  uint8_t is_mesaured_reported;
  hm_receiver_t receivers[5];
  hm_device_t device;

  uint8_t                      state;             /**< Client state. */

  uint16_t w_handle;
  uint16_t r_handle;
  uint16_t r_config_handle;
  uint16_t ping_handle;
  uint16_t ping_config_handle;

  uint16_t w_size;
  uint16_t w_offset;

  uint8_t txrx_buffer[MAX_COMMAND_SIZE];
  bool beginMessageReceived;
  bool escapeNextByte;
  int rx_buffer_ptr;

  //Session
  uint8_t nonce[9];
  uint8_t local_nonce[9];
  uint8_t remote_nonce[9];
  uint16_t local_counter;
  uint16_t remote_counter;
  uint8_t adv_name[8];

  bool isIncoming;
    bool isLink;

  uint8_t public_key_buffer[64];

} connected_beacons_t;

typedef struct
{
  uint16_t major;
  uint16_t minor;
  uint8_t mac[6];
} advertisement_major_minor_t;

typedef struct
{
  uint8_t name[8];
  uint8_t mac[6];
} advertisement_name_t;

static uint8_t is_ctw_call = 0;

static uint8_t reg_serial[9];

static connected_beacons_t mBeacons[MAX_CLIENTS];
static advertisement_major_minor_t mMajorMinor[MAX_CLIENTS];
static advertisement_name_t mName[MAX_CLIENTS];
static uint8_t          m_client_count;

//static dm_application_instance_t    m_dm_app_id;                         /**< Application identifier. */
//static dm_handle_t                  m_dm_device_handle;                  /**< Device Identifier identifier. */
static uint8_t                      m_peer_count = 0;                    /**< Number of peer's connected. */
//static uint8_t                      m_scan_mode;                         /**< Scan mode used by application. */

//static bool                         m_memory_access_in_progress = false; /**< Flag to keep track of ongoing operations on persistent memory. */

#ifdef CAN_HACK
static bool m_send_hack_async = false;
static uint8_t m_send_hack_async_type = 0x00;
static connected_beacons_t  m_hack_p_client;
static bool m_send_hack_empty_round = false;
#endif
static bool m_is_writing_data = false;

static bool m_write_data_failed = false;

//static bool is_ping_srv_found = false;

/**
 * @brief Connection parameters requested for connection.
 */


//static uint16_t r_attribute_handler;

uint8_t hm_bt_core_calculate_next_nonce(uint8_t *nonce){
  uint8_t i = 0;
  for(i = 0; i < 9; i++){
    if(nonce[i] < 0xFF){
      nonce[i]++;
      return 1;
    }
  }

  return 0;
}

void hm_bt_core_encrypt_decrypt(uint8_t *nonce, uint8_t *transaction_nonce, uint8_t *key, uint8_t *data, uint16_t data_size){

  uint8_t random[16];
  uint8_t cipertext[16];

  memcpy(random, nonce, 7);
  memcpy(random + 7, transaction_nonce, 9);

  if(hm_bt_crypto_hal_aes_ecb_block_encrypt(key, random, cipertext) == 0){

    uint8_t xorPosition = 0;

    uint16_t i = 0;
    for(i = 0; i < data_size; i++){

      data[i] = data[i] ^ cipertext[xorPosition];

      xorPosition++;
      if(xorPosition >= 16){
        xorPosition = 0;
      }
    }

    return;
  }
}

uint8_t hm_bt_core_generate_ecdh(uint8_t* nonce, uint8_t *serial, uint8_t *ecdh){
  uint8_t ecdh_o[32];
  hm_bt_crypto_hal_ecc_get_ecdh(serial, ecdh_o);

  uint8_t data_buf[256];
  memset(data_buf,0x00,256);
  memcpy(data_buf,nonce,9);

  return hm_bt_crypto_hal_hmac(ecdh_o, data_buf, ecdh);
}

uint8_t hm_bt_core_generate_hmac(uint8_t* nonce, uint8_t *serial, uint8_t *data, uint16_t size, uint8_t *hmac){
  uint8_t ecdh[32];

  hm_bt_core_generate_ecdh(nonce, serial, ecdh);

  uint8_t data_buf[256];
  memset(data_buf,0x00,256);
  memcpy(data_buf,data,size);

  return hm_bt_crypto_hal_hmac(ecdh, data_buf, hmac);
}

uint8_t hm_bt_core_validate_hmac(uint8_t* nonce, uint8_t *serial, uint8_t *data, uint16_t size, uint8_t *hmac){

  uint8_t hmac_new[32];
  hm_bt_core_generate_hmac(nonce, serial, data, size, hmac_new);

  return memcmp(hmac_new,hmac,32);
}

void hm_bt_core_explode(uint16_t source, uint8_t *dest) {
  dest[0] = source >> 8;
  dest[1] = source & 0xFF;
}

uint16_t hm_bt_core_implode(uint8_t *msb) {
  // msb[1] = lsb
  return (((uint16_t) msb[0]) << 8) | msb[1];
}

void initMajorMinorList(){
  uint8_t i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    memset(mMajorMinor[i].mac,0x00,6);
    memset(mName[i].mac,0x00,6);
  }
}

bool addMajorMinorToList(const uint8_t *mac, uint16_t major, uint16_t minor){

  uint8_t emptyMac[6];
  memset(emptyMac,0x00,6);

  //Find existing
  uint8_t i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(memcmp(mMajorMinor[i].mac,mac,6) == 0){
      /*hm_bt_debug_hal_log("ADD MAJOR MINOR TO LIST");
      hm_bt_debug_hal_log_hex(mac,6);
      hm_bt_debug_hal_log("MAJOR %d",major);
      hm_bt_debug_hal_log("MINOR %d",minor);*/
      mMajorMinor[i].major = major;
      mMajorMinor[i].minor = minor;
      return true;
    }
  }

  //Find empty
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(memcmp(mMajorMinor[i].mac,emptyMac,6) == 0){
      /*hm_bt_debug_hal_log("ADD MAJOR MINOR TO LIST");
      hm_bt_debug_hal_log_hex(mac,6);
      hm_bt_debug_hal_log("MAJOR %d",major);
      hm_bt_debug_hal_log("MINOR %d",minor);*/
      memcpy(mMajorMinor[i].mac,mac,6);
      mMajorMinor[i].major = major;
      mMajorMinor[i].minor = minor;
      return true;
    }
  }

  //hm_bt_debug_hal_log("ADD MAJOR MINOR FAILED");
  return false;
}

bool getMajorMinorFromList(uint8_t *mac, uint16_t *major, uint16_t *minor){

  //Find existing
  uint8_t i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(memcmp(mMajorMinor[i].mac,mac,6) == 0){
      hm_bt_debug_hal_log("GET MAJOR MINOR TO LIST");
      hm_bt_debug_hal_log_hex(mac,6);
      hm_bt_debug_hal_log("MAJOR %d",mMajorMinor[i].major);
      hm_bt_debug_hal_log("MINOR %d",mMajorMinor[i].minor);
      *major = mMajorMinor[i].major;
      *minor = mMajorMinor[i].minor;
      memset(mMajorMinor[i].mac,0x00,6);
      return true;
    }
  }

  hm_bt_debug_hal_log("FAILED GET MAJO MINOR");
  return false;
}

bool addNameToList(const uint8_t *mac, uint8_t *name){

  uint8_t emptyMac[6];
  memset(emptyMac,0x00,6);

  hm_bt_debug_hal_log("ADD NAME TO LIST");
  hm_bt_debug_hal_log_hex(name,8);
  hm_bt_debug_hal_log_hex(mac,6);

  //Find existing
  uint8_t i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(memcmp(mName[i].mac,mac,6) == 0){
      memcpy(mName[i].name,name,8);
      return true;
    }
  }

  //Find empty
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(memcmp(mName[i].mac,emptyMac,6) == 0){
      memcpy(mName[i].mac,mac,6);
      memcpy(mName[i].name,name,8);
      return true;
    }
  }

  hm_bt_debug_hal_log("ADDING NAME FAILED");

  //hm_bt_debug_hal_log("ADD MAJOR MINOR FAILED");
  return false;
}

bool getNameFromList(uint8_t *mac, uint8_t *name){

  hm_bt_debug_hal_log("GET NAME FROM LIST");
  hm_bt_debug_hal_log_hex(mac,6);

  //Find existing
  uint8_t i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(memcmp(mName[i].mac,mac,6) == 0){
      hm_bt_debug_hal_log("NAME");
      hm_bt_debug_hal_log_hex(mName[i].name,8);
      memcpy(name,mName[i].name,8);
      memset(mName[i].mac,0x00,6);
      return true;
    }
  }

  hm_bt_debug_hal_log("FAILED GET NAME FROM LIST");
  return false;
}

static uint32_t get_slot_for_client()
{
    uint32_t i;

    for (i = 0; i < MAX_CLIENTS; i++)
    {
        if (mBeacons[i].isEmpty == true)
        {
            //hm_bt_debug_hal_log("FOUND CLIENT %d",i);
            return i;
        }
    }

    return MAX_CLIENTS;
}

/**@brief Function for service discovery.
 *
 * @param[in] p_client Client context information.
 */
static void service_discover(connected_beacons_t * p_client)
{
    p_client->state = STATE_SERVICE_DISC;

    hm_bt_hal_service_discovery(p_client->device.mac);
}

uint8_t getBeaconId(const uint8_t* mac){
  int i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if( mBeacons[i].isEmpty == false ){
      if(memcmp(mac,mBeacons[i].device.mac,6) == 0){
        return i;
      }
    }
  }

  return 99;
}

uint32_t client_handling_add_serial(uint8_t *mac, uint8_t *serialNumber ){

  uint32_t index = getBeaconId(mac);

  if(index != MAX_CLIENTS){
    connected_beacons_t * p_client;
    p_client = &mBeacons[index];

    memcpy(p_client->device.serial_number,serialNumber,9);
  }

  return NRF_SUCCESS;
}

uint32_t client_handling_set_authorised(connected_beacons_t * p_client, uint8_t authorized ){

  //Set authorized flag and tell to report to CTW
  p_client->device.is_authorised = authorized;
  p_client->is_entered_reported = 0;

  return NRF_SUCCESS;
}

void hm_bt_core_init_slaves();

/**@brief Function for creating a new client.
 */
uint32_t client_handling_create(uint8_t *mac, uint16_t major, uint16_t minor, uint8_t *name, bool isLink)
{
    uint8_t connection_id = get_slot_for_client();

    if(connection_id == MAX_CLIENTS){
      return 1;
    }

    isAuth = true;

    mBeacons[connection_id].state              = STATE_SERVICE_DISC;
    m_client_count++;

    memcpy(mBeacons[connection_id].device.mac,mac,6);

    //Set client initial state
    mBeacons[connection_id].isEmpty = false;
    mBeacons[connection_id].isLeaved = false;
    mBeacons[connection_id].isLink = isLink;

    mBeacons[connection_id].device.major = major;
    mBeacons[connection_id].device.minor = minor;

    memcpy(mBeacons[connection_id].adv_name,name,8);

    service_discover(&mBeacons[connection_id]);

    return NRF_SUCCESS;
}

uint8_t getBeaconIdSerial(const uint8_t* serial){
  int i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    hm_bt_debug_hal_log("FIND CHECK SERIAL");
    hm_bt_debug_hal_log_hex(mBeacons[i].device.serial_number,9);
    if( mBeacons[i].isEmpty == false ){
      if(memcmp(serial,mBeacons[i].device.serial_number,9) == 0){
        return i;
      }
    }
  }

  return 99;
}

uint8_t getBeaconIdName(const uint8_t* name){
  int i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if( mBeacons[i].isEmpty == false ){
      if(memcmp(name,mBeacons[i].adv_name,8) == 0){
        hm_bt_debug_hal_log("FOUND BEACON BY NAME");
        return i;
      }
    }
  }

  return 99;
}

void reportBeaconLeaveForAll(){
  int i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(mBeacons[i].isEmpty == false){
      if(mBeacons[i].isLink == false) {
        hm_bt_debug_hal_log("DISCONNECT CLIENT");
        hm_bt_hal_disconnect(mBeacons[i].device.mac);
      }
    }
  }
}

void reportBeaconExitForAll(){
  int i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    if(mBeacons[i].isEmpty == false){
      if(mBeacons[i].isLink == false) {
        hm_bt_debug_hal_log("EXIT CLIENT");
        mBeacons[i].isLeaved = true;
        mBeacons[i].rssi = 0;
        mBeacons[i].device.is_authorised = 0;
      }
    }
  }
}

void initBeaconList(){
  int i = 0 ;
  for(i = 0 ; i < MAX_CLIENTS ; i++){
    mBeacons[i].isLink = false;
    mBeacons[i].isEmpty = true;
    mBeacons[i].isLeaved = true;
    mBeacons[i].rssi = 0;
    mBeacons[i].device.is_authorised = 0;
    memset(mBeacons[i].device.serial_number,0x00,9);
    memset(mBeacons[i].device.mac,0x00,6);

  }
}

/**@brief Function for freeing up a client by setting its state to idle.
 */
uint32_t client_handling_destroy(const uint8_t *mac)
{
    uint32_t      err_code = NRF_SUCCESS;

    uint8_t id = getBeaconId(mac);

  if(id == 99){
    reportBeaconExitForAll();
    return 1;
  }

    connected_beacons_t    * p_client = &mBeacons[id];

    if (p_client->state != IDLE)
    {
            m_client_count--;
            p_client->state = IDLE;
            p_client->device.is_authorised = 0;
            p_client->isLeaved = true;
    }
    else
    {
        err_code = 1;
    }
    return err_code;
}

//BEacon timing

void checkBeacons(){
  int i = 0 ;
	for(i = 0 ; i < MAX_CLIENTS ; i++){
		if(mBeacons[i].isEmpty == false){

			if(mBeacons[i].isLeaved == true){
        if(mBeacons[i].isEmpty == false){
          mBeacons[i].isEmpty = true;
          mBeacons[i].isLink = false;

          hm_bt_debug_hal_log("EXIT ACTIONS");
          mBeacons[i].rssi = 0;

          //CTW call exit
          hm_ctw_exited_proximity(&mBeacons[i].device);

          mBeacons[i].device.major = 0;
          mBeacons[i].device.minor = 0;
          mBeacons[i].device.is_authorised = 0;
          memset(mBeacons[i].device.serial_number,0x00,9);
          memset(mBeacons[i].device.mac,0x00,6);
        }
			}

      if(mBeacons[i].is_entered_reported  ==  0){
        hm_bt_debug_hal_log("ENTERED ENTERED");
        mBeacons[i].is_entered_reported = 1;
        hm_ctw_entered_proximity(&mBeacons[i].device);
      }
		}
	}
}

void writeNextJunk(uint8_t *mac){


  connected_beacons_t * p_client;
  uint32_t   index;
  index = getBeaconId(mac);

  hm_bt_debug_hal_log("BEACON INDEX %X",index);

  p_client = &mBeacons[index];

  if(m_write_data_failed == true){
    m_write_data_failed = false;
    p_client->w_offset = p_client->w_offset - 20;
  }

  if(p_client->w_size > p_client->w_offset + 20){

    hm_bt_debug_hal_log("WRITE NEXT 20");
    hm_bt_debug_hal_log("OFFSET %d",p_client->w_offset);
    hm_bt_debug_hal_log_hex(p_client->txrx_buffer + p_client->w_offset,20);
    uint32_t   err_code = hm_bt_hal_write_data(mac, 20, p_client->txrx_buffer + p_client->w_offset);
    p_client->w_offset = p_client->w_offset + 20;
    hm_bt_debug_hal_log("WRITE RESULT : %08X",err_code);
    if(err_code != NRF_SUCCESS){
      m_write_data_failed = true;
    }
    //writeNextJunk();
  }else if (p_client->w_size > p_client->w_offset){

    hm_bt_debug_hal_log("WRITE LAST %d",p_client->w_size - p_client->w_offset);
    hm_bt_debug_hal_log("OFFSET %d",p_client->w_offset);
    hm_bt_debug_hal_log_hex(p_client->txrx_buffer + p_client->w_offset,p_client->w_size - p_client->w_offset);
    uint32_t   err_code = hm_bt_hal_write_data(mac, p_client->w_size - p_client->w_offset, p_client->txrx_buffer + p_client->w_offset);
    p_client->w_offset =p_client-> w_offset + ( p_client->w_size - p_client->w_offset );
    hm_bt_debug_hal_log("WRITE RESULT : %08X",err_code);
    if(err_code != NRF_SUCCESS){
      m_write_data_failed = true;
    }else{
      m_is_writing_data = false;
    }
  }
}

uint16_t prepareTxPackage(uint16_t size, uint8_t *data, uint8_t *txrx){
  // Prepare the message, with the appropriate data structure
  uint16_t count = 0;

  txrx[count++] = PACKET_BEGIN;

  int i = 0;
  for (i = 0; i < size; i++) {
    if (data[i] == 0x00 || data[i] == 0xFE || data[i] == 0xFF)
      txrx[count++] = PACKET_ESCAPE;

    txrx[count++] = data[i];
  }

  txrx[count++] = PACKET_END;

  return count;
}

void writeData(uint16_t size, uint8_t *data, uint8_t *mac, bool prepare){

  m_is_writing_data = true;

  hm_bt_debug_hal_log("BT DATA OUT START %d",size);
  hm_bt_debug_hal_log_hex(data,size);

  connected_beacons_t * p_client;
  uint32_t   index;
  index = getBeaconId(mac);

  hm_bt_debug_hal_log("BEACON INDEX %X",index);

  if(index == 99){
    hm_bt_debug_hal_log("INVALID INDEX");
    return;
  }

  p_client = &mBeacons[index];

  if(p_client->device.is_authorised == true){
    if(p_client->isIncoming == true){
      uint8_t ecdh[32];
      hm_bt_core_generate_ecdh(p_client->remote_nonce, p_client->device.serial_number, ecdh);
      hm_bt_core_encrypt_decrypt(p_client->nonce, p_client->remote_nonce, ecdh, data, size);
    }else{
      uint8_t ecdh[32];
      hm_bt_core_generate_ecdh(p_client->local_nonce, p_client->device.serial_number, ecdh);
      hm_bt_core_encrypt_decrypt(p_client->nonce, p_client->local_nonce, ecdh, data, size);
    }
  }

  if(prepare){
    hm_bt_debug_hal_log("PREPARE PACKAGE");
    p_client->w_size = prepareTxPackage(size,data,p_client->txrx_buffer);
  }else{
    p_client->w_size = size;
    memcpy(p_client->txrx_buffer,data,size);
  }

  hm_bt_debug_hal_log("BT DATA OUT %d",p_client->w_size);
  hm_bt_debug_hal_log_hex(p_client->txrx_buffer,p_client->w_size);

  if(p_client->isLink == true){
    hm_bt_debug_hal_log("IS LINK");
    hm_bt_hal_write_data(mac, p_client->w_size, p_client->txrx_buffer);
    return;
  }

  if( p_client->w_size <= 20 ){
    p_client->w_offset = p_client->w_size;
    m_is_writing_data = false;
  }else{
    p_client->w_offset = 20;
  }

  hm_bt_debug_hal_log("W HANDLE %X",p_client->w_handle);

  hm_bt_debug_hal_log("WRITE FIRST %d",p_client->w_offset);
  hm_bt_debug_hal_log_hex(p_client->txrx_buffer,20);
  uint32_t   err_code = hm_bt_hal_write_data(mac, p_client->w_offset, p_client->txrx_buffer);
  hm_bt_debug_hal_log("WRITE RESULT : %X",err_code);
  if(err_code != NRF_SUCCESS){
    m_write_data_failed = true;
  }
}

void sendGetNonceRequest(uint8_t isctw, uint8_t *mac){

  is_ctw_call = isctw;

  uint8_t data[1];

  data[0] = 0x30;

  uint8_t id = getBeaconId(mac);

  if(id != 99){
    mBeacons[id].isIncoming = false;
    writeData(1,data,mac,true);
  }

  //TODO report nonce read failed
}

void sendGetDeviceCertificateRequest(uint8_t isctw, uint8_t *requestData, uint8_t *mac){

  is_ctw_call = isctw;

  uint8_t data[74];

  data[0] = 0x31;
  data[1] = 0x00;
  data[2] = 0x00;
  data[3] = 0x00;
  data[4] = 0x00;
  data[5] = 0x00;
  data[6] = 0x00;
  data[7] = 0x00;
  data[8] = 0x00;
  data[9] = 0x00;

  uint8_t id = getBeaconId(mac);

  if(id != 99){

    mBeacons[id].isIncoming = false;

    if(is_ctw_call == 1){
      memcpy(data + 1,requestData,73);
      writeData(74,data,mac,true);
      return;
    }else{
      //Add signature
      if(hm_bt_crypto_hal_ecc_add_signature(data + 1, 9, data + 10) == 0){

        writeData(74,data,mac,true);
        return;
      }
    }
  }

  hm_bt_debug_hal_log("ERROR");

    //TODO error handling
}

void sendAuthenticate(uint8_t *serial){

  hm_bt_debug_hal_log("AUTH");

  hm_bt_debug_hal_log("USE SERIAL");
  hm_bt_debug_hal_log_hex(serial,9);

  //if authenticated then skip
  uint8_t id = getBeaconIdSerial(serial);

  if(id != 99) {

    hm_bt_debug_hal_log("found");
    if (mBeacons[id].device.is_authorised == 1) {
      mBeacons[id].is_entered_reported = 0;

      hm_bt_debug_hal_log("ALLREADY OK");

      return;
    }

    //Check if we have this device certificate

    hm_certificate_t cert_get;

    if(hm_bt_persistence_hal_get_public_key(serial, cert_get.public_key, cert_get.start_date, cert_get.end_date, &cert_get.permissions_size, cert_get.permissions) != 0){

      //Did not find serial, mark as unknown device

      client_handling_set_authorised(&mBeacons[id],0);

      return;
    }

    uint8_t data[74];

    data[0] = 0x35;

    //Get serial
    if(hm_bt_persistence_hal_get_serial(data + 1) == 0 ){
      //Add signature
      if(hm_bt_crypto_hal_ecc_add_signature(data, 10, data + 10) == 0){

        hm_bt_debug_hal_log("WRITE");

        mBeacons[id].isIncoming = false;

        writeData(74,data,mBeacons[id].device.mac,true);
        return;
      }
    }
  }

  hm_bt_debug_hal_log("ERROR");

    //TODO error handling
}

void sendGetCertificate(uint8_t *mac){

  uint8_t data[74];

  data[0] = 0x34;

  //Get serial
  if(hm_bt_persistence_hal_get_serial(data + 1) == 0 ){
    //Add signature
    if(hm_bt_crypto_hal_ecc_add_signature(data, 10, data + 10) == 0){

      writeData(74,data,mac,true);
      return;
    }
  }

  hm_bt_debug_hal_log("ERROR");

  //TODO error handling

}

void sendRegisterCertificate(uint8_t isctw, uint8_t *certData, uint8_t size, uint8_t *serial){

  uint8_t id = getBeaconIdSerial(serial);

  if(id != 99){
    //Stop scanning
    hm_bt_hal_scan_stop();

    is_ctw_call = isctw;

    uint8_t publicKey[64];
    hm_cert_get_providing_serial(certData, reg_serial);
    hm_cert_get_gaining_public_key(certData, publicKey);

    hm_bt_debug_hal_log("PUBLIC TO BEACON");
    hm_bt_debug_hal_log_hex(publicKey,64);

    uint8_t data[180];

    data[0] = 0x32;
    memcpy(data + 1, certData,size);

    mBeacons[id].isIncoming = false;
    writeData(size + 1,data,mBeacons[id].device.mac,true);
  }
}

void sendStoreCertificate(uint8_t *mac){
  uint8_t data[158] = {0x32, 0x01, 0x23, 0x2E, 0x08, 0xD7, 0x2C, 0xA5, 0x71, 0xEE, 0xFF, 0x63, 0x82, 0x45, 0xCE, 0x9D, 0x16, 0xAD, 0xD6, 0x73, 0x63, 0xF8, 0x55, 0xC3, 0xF7, 0x7A, 0xC9, 0xD7, 0x64, 0x5B, 0xA7, 0x22, 0x86, 0xDE, 0x8C, 0x56, 0xE4, 0xD0, 0xB0, 0xD8, 0x0C, 0x1C, 0x25, 0x83, 0xD5, 0xB2, 0xF5, 0xBC, 0x39, 0x22, 0x03, 0x51, 0x91, 0xE8, 0x29, 0x83, 0xCC, 0x12, 0x5A, 0xA6, 0xCD, 0xE5, 0x31, 0xB4, 0xD8, 0x35, 0xA4, 0x42, 0xCC, 0xA7, 0xF0, 0x2E, 0x7A, 0xD5, 0x01, 0x23, 0x4D, 0x3A, 0xD6, 0x2C, 0xA5, 0x71, 0xEE, 0x0F, 0x05, 0x08, 0x0E, 0x27, 0x0F, 0x05, 0x09, 0x0E, 0x27, 0x00, 0xF6, 0x5C, 0x9F, 0x08, 0x10, 0x97, 0x45, 0xD9, 0xA2, 0x1A, 0x88, 0x98, 0xA4, 0x5C, 0x13, 0xAC, 0xE7, 0xDA, 0x07, 0x80, 0xC7, 0x30, 0xD4, 0x6E, 0x6B, 0x6A, 0x0D, 0x57, 0x7F, 0xFB, 0xD3, 0xE8, 0xD1, 0xC0, 0x3C, 0x04, 0x8B, 0xC9, 0x52, 0x70, 0x42, 0x30, 0xE6, 0xA8, 0x74, 0x9F, 0xB9, 0x56, 0x86, 0xE6, 0x1D, 0x3A, 0x42, 0x70, 0x15, 0x3B, 0x02, 0xE0, 0xEC, 0x7C, 0x49, 0x03, 0xB2, 0x4D };

  hm_cert_get_providing_serial(data + 1, reg_serial);

  uint32_t id = getBeaconId(mac);

  mBeacons[id].isIncoming = false;

  writeData(158,data,mac,true);
}

void sendRevoke(uint8_t *serial){

  hm_bt_debug_hal_log("REVOKE SERIAL");
  hm_bt_debug_hal_log_hex(serial,9);

  uint8_t id = getBeaconIdSerial(serial);

  if(id != 99){
    uint8_t data[42];
    data[0] = 0x38;

    //Get serial
    if(hm_bt_persistence_hal_get_serial(data + 1) == 0 ){
      //Add hmac
      if( hm_bt_core_generate_hmac(mBeacons[id].local_nonce, mBeacons[id].device.serial_number, data, 10, data + 1 + 9)== 0 ){

        hm_bt_debug_hal_log("REVOKE");
        hm_bt_debug_hal_log_hex(data,42);

        mBeacons[id].isIncoming = false;
        writeData(42,data,mBeacons[id].device.mac,true);
        return;
      }
    }
  }

  hm_bt_debug_hal_log("ERROR");

  //TODO error handling
}

void sendSecureContainer(uint8_t *serial, uint8_t *dataBuffer, uint8_t size){
  hm_bt_debug_hal_log("SECURE CONTAINER OUT");
  hm_bt_debug_hal_log_hex(serial,9);

  uint8_t id = getBeaconIdSerial(serial);

  if(id != 99){

    hm_bt_debug_hal_log_hex(mBeacons[id].nonce,9);
    hm_bt_debug_hal_log_hex(mBeacons[id].local_nonce,9);

    uint8_t data[290];
    data[0] = 0x36;
    data[1] = 0x01;
    data[2] = size;

    memcpy(data + 3, dataBuffer, size);

    //Add hmac
    if( hm_bt_core_generate_hmac(mBeacons[id].local_nonce, mBeacons[id].device.serial_number, data, 3 + size, data + 3 + size) == 0 ){
      mBeacons[id].isIncoming = false;
      writeData(3 + size + 32,data,mBeacons[id].device.mac,true);
      return;
    }
  }

  hm_bt_debug_hal_log("ERROR");

  //TODO error handling
}

void processGetNonce(connected_beacons_t * p_client){

  memcpy(p_client->nonce,p_client->txrx_buffer + 2,9);

  if(is_ctw_call == 1){

    uint8_t nonce[9];
    //uint8_t error = 0;

    if(p_client->txrx_buffer[0] == 0x01){
      memcpy(nonce,p_client->txrx_buffer + 2,9);
    }else{
      //error = 1;
    }

    is_ctw_call = 0;
    //hm_ctw_device_nonce_read(&p_client->device, nonce, error);
    return;
  }

  hm_bt_debug_hal_log("NOT CTW");

  if(p_client->txrx_buffer[0] == 0x01){
    sendGetDeviceCertificateRequest(0,NULL,p_client->device.mac);
  }else{
    //TODO add to skip list
    //AddBeaconTolist( cur_mac, 0, cur_serial );
  }
}

void processGetDeviceCertificate(connected_beacons_t * p_client){

  //uint8_t error = 0;

  if(p_client->txrx_buffer[0] == 0x01){

    if(hm_bt_crypto_hal_ecc_validate_ca_signature(p_client->txrx_buffer + 2, 89, p_client->txrx_buffer + 2 + 89) == 0){

      hm_bt_debug_hal_log("SERIAL SIG OK");
      //Add serial number to device
      //memcpy(p_client->device.serial_number,p_client->txrx_buffer + 2,9);
      client_handling_add_serial(p_client->device.mac,p_client->txrx_buffer + 2 + 4 + 12);
      memcpy(p_client->public_key_buffer,p_client->txrx_buffer + 2 + 4 + 12 + 9,64);

      //If ctw call then only start auth when not authenticated before
      if(is_ctw_call == 1){

        hm_bt_debug_hal_log("CTW");

        is_ctw_call = 0;

        if(p_client->device.is_authorised == 0){
          sendAuthenticate(p_client->device.serial_number);
        }

      }else{
        sendAuthenticate(p_client->device.serial_number);
      }

      return;

    }
  }

  hm_bt_debug_hal_log("ERROR");
  hm_bt_debug_hal_log("GET DEVICE CERTIFICATE");

  if(hm_ctw_get_device_certificate_failed(&p_client->device, p_client->nonce) == 0){
    //Add to list as unauthenticated
    client_handling_set_authorised(p_client,0);
    hm_bt_hal_scan_start();
  }
}

void processGetCertificate(connected_beacons_t * p_client){
  hm_bt_debug_hal_log("PROCESS GET CERTIFICATE");

  if(p_client->txrx_buffer[0] == 0x01){

    uint8_t permissionsSize = 0;
    uint8_t permissions[16];
    uint8_t signature[64];
    uint8_t gainingPublicKey[64];
    uint8_t gainingSerial[9];
    uint8_t providingSerial[9];
    uint8_t startDate[5];
    uint8_t endDate[5];

    hm_cert_get_permissions(p_client->txrx_buffer + 2, &permissionsSize,permissions);
    hm_cert_get_signature(p_client->txrx_buffer + 2, signature);
    hm_cert_get_providing_serial(p_client->txrx_buffer + 2, providingSerial);
    hm_cert_get_gaining_public_key(p_client->txrx_buffer + 2, gainingPublicKey);
    hm_cert_get_gaining_serial(p_client->txrx_buffer + 2, gainingSerial);
    hm_cert_get_start_date(p_client->txrx_buffer + 2, startDate);
    hm_cert_get_end_date(p_client->txrx_buffer + 2, endDate);

    uint8_t serial[9];

    hm_bt_debug_hal_log("GOT CERT");
    hm_cert_print(p_client->txrx_buffer + 2);

    //Get serial for verify
    if(hm_bt_persistence_hal_get_serial(serial) == 0 ){

      //Check if serial is OK
      if(memcmp(serial,providingSerial,9) == 0 ){

        //Check if CA signature is ok
        if(hm_bt_crypto_hal_ecc_validate_ca_signature(p_client->txrx_buffer + 2, 92 + 1 + permissionsSize, signature) == 0){

          //Store public key
          if(hm_bt_persistence_hal_add_public_key(gainingSerial, gainingPublicKey, startDate, endDate, permissionsSize, permissions) == 0 ){

            hm_bt_debug_hal_log("DONE");

          }else{
            hm_bt_debug_hal_log("STORAGE FULL");
          }
        }else{
          hm_bt_debug_hal_log("INVALID SIGNATURE");
        }
      }else{
        hm_bt_debug_hal_log("INVALID DATA");
      }
    }
  }

  sendGetNonceRequest(0,p_client->device.mac);
}

void processAuthenticate(connected_beacons_t * p_client){

  hm_bt_debug_hal_log("AUTH");

  if(p_client->txrx_buffer[0] == 0x01){

    hm_bt_debug_hal_log("INCOMING");
    hm_bt_debug_hal_log_hex(p_client->txrx_buffer,75);

    hm_bt_debug_hal_log("SERIAL");
    hm_bt_debug_hal_log_hex(p_client->device.serial_number,9);

    if( hm_bt_crypto_hal_ecc_validate_signature(p_client->txrx_buffer, 11, p_client->txrx_buffer + 11, p_client->device.serial_number) == 0 ){

      hm_bt_debug_hal_log("SIGNATURE OK");

      //Create shared key
      uint8_t ecdh[32];

      if(hm_bt_core_generate_ecdh(p_client->txrx_buffer + 2, p_client->device.serial_number, ecdh) == 0){

        memcpy(p_client->nonce,p_client->txrx_buffer + 2,9);
        memcpy(p_client->local_nonce,p_client->nonce,9);
        memcpy(p_client->remote_nonce,p_client->nonce,9);
        p_client->local_counter = 0;
        p_client->remote_counter = 0;

        hm_bt_debug_hal_log("OK");
        hm_bt_debug_hal_log("ECDH");
        hm_bt_debug_hal_log_hex(ecdh,32);

        client_handling_set_authorised(p_client,1);

#ifdef CAN_HACK
        m_send_hack_async = true;
        m_send_hack_async_type = 0x02;
        m_hack_p_client = *p_client;
#endif

      }else{
        client_handling_set_authorised(p_client,0);
      }
    }else{
      client_handling_set_authorised(p_client,0);
    }
  }else{
    client_handling_set_authorised(p_client,0);
  }

  //Start to scan others
  //TODO PARROT
  hm_bt_hal_scan_start();
}

void processRegisterCertificate(connected_beacons_t * p_client){

  hm_bt_debug_hal_log("REGISTRATION RESPONSE");


  if(p_client->txrx_buffer[0] == 0x01){

    //if( hm_bt_crypto_hal_ecc_validate_signature(p_client->txrx_buffer, 66, p_client->txrx_buffer + 66, p_client->device.serial_number) == 0 )
    {

      uint8_t permissionsSize = 0;
      uint8_t permissions[16];
      uint8_t startDate[5];
      uint8_t endDate[5];

      //Store public key

      hm_bt_debug_hal_log("FROM BLE KEY");
      hm_bt_debug_hal_log_hex(p_client->txrx_buffer + 2,64);

      if(hm_bt_persistence_hal_add_public_key(p_client->txrx_buffer + 2, p_client->device.serial_number, startDate, endDate, permissionsSize, permissions) == 0 ){

        hm_bt_debug_hal_log("REG DONE");
        hm_ctw_device_certificate_registered(&p_client->device,p_client->txrx_buffer + 2,0);

      }else{
        hm_bt_debug_hal_log("REG STORAGE FULL");
      }

      return;
    }
  }

  hm_bt_debug_hal_log("REG FAILED");
}

void processGetUserFeedback(connected_beacons_t * p_client){

  uint8_t ecdh[32];
  hm_bt_core_generate_ecdh(p_client->local_nonce, p_client->device.serial_number, ecdh);

  //uint8_t feedback = 0;

  hm_bt_core_calculate_next_nonce(p_client->local_nonce);
  p_client->local_counter++;

  if(is_ctw_call == 1){

    if(p_client->txrx_buffer[0] == 0x01){
      //feedback = 1;
    }

    //hm_ctw_user_feedback_received(&p_client->device, p_client->txrx_buffer[2], feedback, 0);

    is_ctw_call = 0;
  }
}

void processRevoke(connected_beacons_t * p_client){
  uint8_t ecdh[32];
  hm_bt_core_generate_ecdh(p_client->local_nonce, p_client->device.serial_number, ecdh);
  if(p_client->txrx_buffer[0] == 0x01){
    hm_bt_persistence_hal_remove_public_key(p_client->device.serial_number);

    hm_bt_debug_hal_log("BEACON PUBLIC REMOVED");

    client_handling_set_authorised(p_client,0);

    //sd_ble_gap_disconnect(p_client->srv_db.conn_handle,0x13);
  }
}

void sendError(connected_beacons_t * p_client, uint8_t error, uint8_t command) {
  uint8_t data[3];
  data[0] = ID_ERROR;
  data[1] = command;
  data[2] = error;

  writeData(3,data,p_client->device.mac,true);
}

void processSecureContainer(connected_beacons_t * p_client){

hm_bt_debug_hal_log("Secure container");

  hm_bt_core_calculate_next_nonce(p_client->local_nonce);
  p_client->local_counter++;

#ifdef CAN_HACK
  m_send_hack_async = true;
  m_send_hack_async_type = 0x02;
  m_hack_p_client = *p_client;
#endif

  /*hm_bt_hal_delay_ms(1000);

  sendGetUserFeedback(false,0x00,p_client->device.serial_number);*/
}

void processSecureCommandContainerIncoming(connected_beacons_t * p_client){

  hm_bt_debug_hal_log_hex(p_client->nonce,9);
  hm_bt_debug_hal_log_hex(p_client->remote_nonce,9);

  if(hm_bt_core_validate_hmac(p_client->remote_nonce,p_client->device.serial_number,p_client->txrx_buffer, 1 + 1 + 1 + p_client->txrx_buffer[2] , p_client->txrx_buffer + 1 + 1 + 1 + p_client->txrx_buffer[2]) == 0){

    uint8_t error = 0;
    uint8_t size = p_client->txrx_buffer[2];
    uint8_t dataBuffer[255];
    uint8_t withHMAC = p_client->txrx_buffer[1];
    memcpy(dataBuffer,p_client->txrx_buffer + 1 + 1 + 1,size);

    hm_bt_debug_hal_log("size %d",size);

    hm_ctw_command_received(&p_client->device, dataBuffer, &size, &error);

    uint8_t data[290];
    data[0] = 0x01;
    data[1] = ID_CRYPTO_CONTAINER;

    hm_bt_debug_hal_log("size %d",size);

    data[2] = size;

    memcpy(data + 3,dataBuffer,size);

    p_client->isIncoming = true;

    if(withHMAC == 1){
      hm_bt_core_generate_hmac(p_client->remote_nonce, p_client->device.serial_number, data, 3 + size, data + 3 + size);

      hm_bt_debug_hal_log("RESPONSE");
      hm_bt_debug_hal_log_hex(data,3 + size + 32);

      writeData(3 + size + 32,data,p_client->device.mac,true);
    }else{
      hm_bt_debug_hal_log("RESPONSE");
      hm_bt_debug_hal_log_hex(data,3 + size + 32);

      writeData(3 + size,data,p_client->device.mac,true);
    }

    hm_bt_core_calculate_next_nonce(p_client->remote_nonce);
    p_client->remote_counter++;

    if(data[3] == 0x01 && data[4] == 0x04){
      hm_bt_debug_hal_log("DISCONNECT");
      hm_bt_hal_disconnect(p_client->device.mac);
    }
    if(data[3] == 0x01 && data[4] == 0x01 && data[5] == 0x00){
      hm_bt_debug_hal_log("DISCONNECT");
      hm_bt_hal_disconnect(p_client->device.mac);
    }
#ifdef CAN_HACK
    //TODO async response hack for testingrr
    if(dataBuffer[0] == 0x01 && dataBuffer[1] == 0x03){

      m_send_hack_async = true;
      m_send_hack_async_type = 0x02;
      m_hack_p_client = *p_client;
    }
    if(dataBuffer[0] == 0x01 && dataBuffer[1] == 0x04){

      m_send_hack_async = true;
      m_send_hack_async_type = 0x05;

      m_hack_p_client = *p_client;
    }
#endif

    return;
  }

  sendError(p_client, ERR_INVALID_HMAC, p_client->txrx_buffer[0]);
}

void processGetNonceIncoming(connected_beacons_t * p_client){

  hm_bt_debug_hal_log("INCOMING GET NONCE");

  if(hm_bt_crypto_hal_generate_nonce(p_client->txrx_buffer + 2) == 0){

    hm_bt_debug_hal_log("NEW NONCE GENERATED");

    uint8_t data[11];

    data[0] = ID_ACK_COMMAND;
    data[1] = ID_CRYPTO_GET_NONCE;

    writeData(11,data,p_client->device.mac,true);
    return;
  }

  sendError(p_client, ERR_INTERNAL_ERROR, ID_CRYPTO_GET_NONCE);
}

void processGetDeviceCertificateIncoming(connected_beacons_t * p_client){

  uint8_t zero[9] = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

  if(memcmp(p_client->txrx_buffer+1,zero,9) != 0){
    //Validate CA signature
    if(hm_bt_crypto_hal_ecc_validate_ca_signature(p_client->txrx_buffer+1, 9, p_client->txrx_buffer + 10) != 0){

      sendError(p_client, ERR_INVALID_SIGNATURE, ID_CRYPTO_GET_DEVICE_CERTIFICATE);
      return;
    }
  }else{
    //Validate local signature
    if(hm_bt_crypto_hal_ecc_validate_all_signatures(p_client->txrx_buffer+1, 9, p_client->txrx_buffer + 10) != 0){

      sendError(p_client, ERR_INVALID_SIGNATURE, ID_CRYPTO_GET_DEVICE_CERTIFICATE);
      return;
    }
  }

  //Get device certificate for response

  uint8_t deviceCertificate[153] = {
          0x48, 0x49, 0x4D, 0x4C,
          0xBB, 0x81, 0x49, 0xAB, 0xBE, 0x90, 0x6F, 0x25, 0xD7, 0x16, 0xE8, 0xDE,
          0x01, 0x23, 0x19, 0x10, 0xD6, 0x2C, 0xA5, 0x71, 0xEE,
          0x2C, 0x28, 0xED, 0x91, 0x9C, 0x31, 0x53, 0x6E, 0x5B, 0x7C, 0x11, 0x88,
          0x96, 0x01, 0x9E, 0xC5, 0x18, 0x84, 0x24, 0x22, 0x0B, 0x1F, 0xFF, 0x95,
          0xEB, 0x6C, 0x0C, 0x6E, 0x99, 0x26, 0x1C, 0x5F, 0x90, 0x65, 0x23, 0xC0,
          0x86, 0x88, 0xD6, 0xC1, 0xBD, 0x1E, 0x54, 0x0A, 0x21, 0xA7, 0xAD, 0x14,
          0x07, 0xB0, 0x30, 0x41, 0xBD, 0x00, 0x66, 0xB8, 0xCC, 0x93, 0xE1, 0x29,
          0xA3, 0x2F, 0x69, 0x82,
          0x4B, 0xEB, 0x06, 0x76, 0xFD, 0xA9, 0xBC, 0x26, 0xE5, 0x7F, 0x8D, 0xF6, 0xFA, 0x98, 0x55, 0x93, 0xE1, 0xAA, 0xEA, 0xB4, 0x46, 0x07, 0x41, 0x23, 0x8C, 0x0A, 0xE8, 0x02, 0x67, 0xA6, 0x20, 0x82, 0x85, 0x15, 0x93, 0x91, 0x3A, 0xB3, 0xA5, 0x40, 0x8A, 0xC6, 0xF4, 0x2C, 0xCF, 0xDE, 0xDC, 0x82, 0xA9, 0xB8, 0x61, 0x49, 0x5B, 0xF0, 0x1F, 0xAB, 0x8E, 0xA1, 0x1F, 0x4C, 0x23, 0x5C, 0x8C, 0x04
  };

  uint8_t data[155];

  data[0] = ID_ACK_COMMAND;
  data[1] = ID_CRYPTO_GET_DEVICE_CERTIFICATE;
  memcpy(data+2,deviceCertificate,153);
  writeData(155,data,p_client->device.mac,true);
}

void processRegisterCertificateIncoming(connected_beacons_t * p_client){

  hm_cert_print(p_client->txrx_buffer + 1);

  uint8_t permissionsSize = 0;
  uint8_t permissions[16];
  uint8_t signature[64];
  uint8_t gainingPublicKey[64];
  uint8_t gainingSerial[9];
  uint8_t providingSerial[9];
  uint8_t startDate[5];
  uint8_t endDate[5];

  hm_cert_get_permissions(p_client->txrx_buffer + 1, &permissionsSize,permissions);
  hm_cert_get_signature(p_client->txrx_buffer + 1, signature);
  hm_cert_get_providing_serial(p_client->txrx_buffer + 1, providingSerial);
  hm_cert_get_gaining_public_key(p_client->txrx_buffer + 1, gainingPublicKey);
  hm_cert_get_gaining_serial(p_client->txrx_buffer + 1, gainingSerial);
  hm_cert_get_start_date(p_client->txrx_buffer + 1, startDate);
  hm_cert_get_end_date(p_client->txrx_buffer + 1, endDate);

  uint8_t serial[9];

  //Get serial for verify
  if(hm_bt_persistence_hal_get_serial(serial) == 0 ){

    //Check if serial is OK
    if(memcmp(serial,providingSerial,9) == 0 ){

      //Check if CA signature is ok
      if(hm_bt_crypto_hal_ecc_validate_ca_signature(p_client->txrx_buffer + 1, 92 + 1 + permissionsSize, signature) == 0){

        //Store public key
        if(hm_bt_persistence_hal_add_public_key(gainingSerial, gainingPublicKey, startDate, endDate, permissionsSize, permissions) == 0 ){

          //TODO add user feedback
          uint8_t data[130];

          //Get publick key for response
          if(hm_bt_persistence_hal_get_local_public_key(data + 2) == 0){

            data[0] = ID_ACK_COMMAND;
            data[1] = ID_CRYPTO_REGISTER_CERTIFICATE;

            //Add signature
            if(hm_bt_crypto_hal_ecc_add_signature(data, 66, data + 66) == 0){

              writeData(130,data,p_client->device.mac,true);
              return;
            }
          }
        }else{
          sendError(p_client, ERR_STORAGE_FULL, ID_CRYPTO_REGISTER_CERTIFICATE);
          return;
        }
      }else{
        sendError(p_client, ERR_INVALID_SIGNATURE, ID_CRYPTO_REGISTER_CERTIFICATE);
        return;
      }
    }else{
      sendError(p_client, ERR_INVALID_DATA, ID_CRYPTO_REGISTER_CERTIFICATE);
      return;
    }
  }

  sendError(p_client, ERR_TIMEOUT, ID_CRYPTO_REGISTER_CERTIFICATE); //TODO invalid token
  return;
}

void processStoreCertificateIncoming(connected_beacons_t * p_client){

  hm_cert_print(p_client->txrx_buffer + 1);

  uint8_t permissionsSize = 0;
  uint8_t permissions[16];
  uint8_t signature[64];
  uint8_t certSize = 0;

  certSize = hm_cert_size(p_client->txrx_buffer + 1);
  hm_cert_get_permissions(p_client->txrx_buffer + 1, &permissionsSize,permissions);
  hm_cert_get_signature(p_client->txrx_buffer + 1, signature);

  //Validate hmac
  if(hm_bt_core_validate_hmac(p_client->remote_nonce,p_client->device.serial_number,p_client->txrx_buffer, 1 + certSize + 64 , p_client->txrx_buffer + 1 + certSize + 64) == 0){
    if(hm_bt_crypto_hal_ecc_validate_ca_signature(p_client->txrx_buffer + 1, 92 + 1 + permissionsSize, signature) == 0){

      if(hm_bt_persistence_hal_add_stored_certificate(p_client->txrx_buffer + 1, 92 + 1 + permissionsSize + 64) == 0){

        uint8_t data[2];

        data[0] = ID_ACK_COMMAND;
        data[1] = ID_CRYPTO_STORE_CERTIFICATE;

        writeData(2,data,p_client->device.mac,true);
        return;
      }

    }else{
      sendError(p_client, ERR_INVALID_SIGNATURE, ID_CRYPTO_STORE_CERTIFICATE);
      return;
    }
  }else{
    sendError(p_client, ERR_INVALID_HMAC, ID_CRYPTO_STORE_CERTIFICATE);
    return;
  }

  sendError(p_client, ERR_INTERNAL_ERROR, ID_CRYPTO_STORE_CERTIFICATE);
  return;
}

void processGetCertificateIncoming(connected_beacons_t * p_client){

  uint8_t data[300];

  data[0] = ID_ACK_COMMAND;
  data[1] = ID_CRYPTO_GET_CERTIFICATE;

  uint8_t certificate[180];
  uint16_t certSize = 0;

  uint8_t providingSerial[9];

  hm_bt_persistence_hal_get_stored_certificate(certificate, &certSize);
  certSize = hm_cert_size(certificate);
  hm_cert_get_providing_serial(certificate, providingSerial);

  if(memcmp(p_client->txrx_buffer + 1,providingSerial,9) == 0 ){

    memcpy(data + 2, certificate,certSize + 64);

    writeData(certSize + 64 + 2,data,p_client->device.mac,true);

    hm_bt_persistence_hal_erase_stored_certificate();

    return;

  }

  sendError(p_client, ERR_INVALID_DATA, ID_CRYPTO_GET_CERTIFICATE);
  return;
}

void processAuthenticateIncoming(connected_beacons_t * p_client){

  //Validate signature
  hm_bt_debug_hal_log("INCOMING AUTH");
  hm_bt_debug_hal_log_hex(p_client->txrx_buffer + 1,9);
  if(hm_bt_crypto_hal_ecc_validate_signature(p_client->txrx_buffer, 10, p_client->txrx_buffer + 10, p_client->txrx_buffer + 1) == 0){

    //TODO memcpy(connected_serial,p_client->txrx_buffer + 1,9);

    uint8_t data[75];

    //Get nonce for response
    if(hm_bt_crypto_hal_generate_nonce(p_client->nonce) == 0){
      memcpy(data + 2,p_client->nonce,9);
      memcpy(p_client->local_nonce,p_client->nonce,9);
      memcpy(p_client->remote_nonce,p_client->nonce,9);
      //Create shared key
      uint8_t ecdh[32];
      if(hm_bt_crypto_hal_ecc_get_ecdh(p_client->txrx_buffer + 1, ecdh) == 0){

        hm_bt_debug_hal_log("ECDH");
        hm_bt_debug_hal_log_hex(ecdh,32);

        data[0] = ID_ACK_COMMAND;
        data[1] = ID_CRYPTO_AUTHENTICATE;

        //Add signature
        if(hm_bt_crypto_hal_ecc_add_signature(data, 11, data + 11) == 0){

          writeData(75,data,p_client->device.mac,true);

          p_client->device.is_authorised = true;

          return;
        }
      }
    }
  }else{
    sendError(p_client, ERR_INVALID_SIGNATURE, ID_CRYPTO_AUTHENTICATE);
    return;
  }

  sendError(p_client, ERR_INTERNAL_ERROR, ID_CRYPTO_AUTHENTICATE); //TODO invalid token
  return;
}

void processRevokeIncoming(connected_beacons_t * p_client){

  if(hm_bt_core_validate_hmac(p_client->remote_nonce,p_client->device.serial_number,p_client->txrx_buffer, 10, p_client->txrx_buffer + 10) == 0){

    if(hm_bt_persistence_hal_remove_public_key(p_client->txrx_buffer + 1) == 0){

      uint8_t data[2];

      data[0] = ID_ACK_COMMAND;
      data[1] = ID_CRYPTO_REVOKE;

      writeData(2,data,p_client->device.mac,true);

      hm_bt_hal_disconnect(p_client->device.mac);

      return;
    }
  }

  sendError(p_client, ERR_INVALID_HMAC, ID_CRYPTO_REVOKE); //TODO invalid token
  return;
}

void processIncomingPacket( connected_beacons_t * p_client) {

  if(p_client->device.is_authorised == true){
    if(p_client->isIncoming == true){
      uint8_t ecdh[32];
      hm_bt_core_generate_ecdh(p_client->remote_nonce, p_client->device.serial_number, ecdh);
      hm_bt_debug_hal_log_hex(ecdh,32);
      hm_bt_core_encrypt_decrypt(p_client->nonce, p_client->remote_nonce, ecdh, p_client->txrx_buffer, p_client->rx_buffer_ptr + 1);
    }else{
      uint8_t ecdh[32];
      hm_bt_core_generate_ecdh(p_client->local_nonce, p_client->device.serial_number, ecdh);
      hm_bt_debug_hal_log_hex(ecdh,32);
      hm_bt_core_encrypt_decrypt(p_client->nonce, p_client->local_nonce, ecdh, p_client->txrx_buffer, p_client->rx_buffer_ptr + 1);
    }
  }

  if (p_client->beginMessageReceived) {
    switch (p_client->txrx_buffer[0]) {
      case ID_CRYPTO_GET_NONCE:
        p_client->isIncoming = true;
        processGetNonceIncoming(p_client);
            break;
      case ID_CRYPTO_GET_DEVICE_CERTIFICATE:
        p_client->isIncoming = true;
        processGetDeviceCertificateIncoming(p_client);
            break;
      case ID_CRYPTO_REGISTER_CERTIFICATE:
        p_client->isIncoming = true;
        processRegisterCertificateIncoming(p_client);
            break;
      case ID_CRYPTO_STORE_CERTIFICATE:
        p_client->isIncoming = true;
        processStoreCertificateIncoming(p_client);
            break;
      case ID_CRYPTO_GET_CERTIFICATE:
        p_client->isIncoming = true;
        processGetCertificateIncoming(p_client);
            break;
      case ID_CRYPTO_AUTHENTICATE:
        p_client->isIncoming = true;
        processAuthenticateIncoming(p_client);
            break;
      case ID_CRYPTO_CONTAINER:
        p_client->isIncoming = true;
        processSecureCommandContainerIncoming(p_client);
            break;
      case ID_CRYPTO_REVOKE:
        p_client->isIncoming = true;
        processRevokeIncoming(p_client);
            break;
      case ID_ACK_COMMAND:
      case ID_ERROR: {
        switch (p_client->txrx_buffer[1]) {
          case ID_CRYPTO_GET_NONCE:
            processGetNonce(p_client);
                break;
          case ID_CRYPTO_GET_DEVICE_CERTIFICATE:
            processGetDeviceCertificate(p_client);
                break;
          case ID_CRYPTO_GET_CERTIFICATE:
            processGetCertificate(p_client);
                break;
          case ID_CRYPTO_AUTHENTICATE:
            processAuthenticate(p_client);
                break;
          case ID_CRYPTO_REGISTER_CERTIFICATE:
            processRegisterCertificate(p_client);
                break;
          case ID_REQUEST_USER_FEEDBACK:
            processGetUserFeedback(p_client);
                break;
          case ID_CRYPTO_REVOKE:
            processRevoke(p_client);
                break;
          case ID_CRYPTO_CONTAINER:
            processSecureContainer(p_client);
                break;
          default:
            //TODO add to skip list
            //AddBeaconTolist( cur_mac, 0, cur_serial );
            hm_bt_debug_hal_log("UNKNOWN PACKAGE");
                break;
        }
        break;
      }
      default:
        sendError(p_client, ERR_COMMAND_UNKNOWN, p_client->txrx_buffer[0]);
            break;
    }
  }
}

void resetRxBuffer(connected_beacons_t * p_client) {
  p_client->rx_buffer_ptr = 0;
  p_client->beginMessageReceived = false;
  p_client->escapeNextByte = false;
}

bool bt_data_handler(const uint8_t * p_data, uint16_t length, connected_beacons_t * p_client)
{
  int i = 0;
	for (i = 0; i < length; i++) {
	    bool escape = p_client->escapeNextByte;
	    p_client->escapeNextByte = false;

	    // End of message reached
	    if (!escape && p_data[i] == PACKET_END) {
	      processIncomingPacket(p_client);
	      resetRxBuffer(p_client);

	      return true;

	      continue;
	    }

	    // Escape next byte
	    if (!escape && p_data[i] == PACKET_ESCAPE) {
	      p_client->escapeNextByte = true;
	      continue;
	    }

      // Skip begin message byte
      if (!escape && p_data[i] == PACKET_BEGIN) {
        resetRxBuffer(p_client);
        p_client->beginMessageReceived = true;
        continue;
      }

	    // Check for overflow
	    if (p_client->rx_buffer_ptr >= MAX_COMMAND_SIZE) {
	      if (p_client->beginMessageReceived) {
	        // send appropriate nack
	      }
	      resetRxBuffer(p_client);
	      continue;
	    }

	    // Write byte in buffer
	    p_client->txrx_buffer[p_client->rx_buffer_ptr] = p_data[i];
	    p_client->rx_buffer_ptr++;
	  }

	return false;
}

void hm_bt_core_sensing_connect(uint8_t *mac){
    hm_bt_debug_hal_log("[APPL]: >> DM_EVT_CONNECTION\r\n");

    hm_bt_debug_hal_log("[APPL]:[%02X %02X %02X %02X %02X %02X]: Connection Established\r\n",
                        mac[0], mac[1], mac[2],
                        mac[3], mac[4], mac[5]);

    uint16_t major = 0;
    uint16_t minor = 0;
    getMajorMinorFromList(mac,&major,&minor);

    uint8_t name[8];
    getNameFromList(mac,name);

    client_handling_create(mac,major,minor,name,false);

    m_peer_count++;
    hm_bt_debug_hal_log("[APPL]: << DM_EVT_CONNECTION\r\n");
}

void hm_bt_core_sensing_disconnect(uint8_t *mac){
    done = false;
    hm_bt_debug_hal_log("[APPL]: >> DM_EVT_DISCONNECTION\r\n");

    client_handling_destroy(mac);
    //APP_ERROR_CHECK(err_code);

    //if(BLE_ON != 0){
      hm_bt_hal_scan_start();
    //}

    if(m_peer_count != 0){
      m_peer_count--;
    }

    hm_bt_debug_hal_log("[APPL]: << DM_EVT_DISCONNECTION\r\n");
}

/**
 * @brief Parses advertisement data, providing length and location of the field in case
 *        matching data is found.
 *
 * @param[in]  Type of data to be looked for in advertisement data.
 * @param[in]  Advertisement report length and pointer to report.
 * @param[out] If data type requested is found in the data report, type data length and
 *             pointer to data will be populated here.
 *
 * @retval NRF_SUCCESS if the data type is found in the report.
 * @retval NRF_ERROR_NOT_FOUND if the data type could not be found.
 */
static uint32_t adv_report_parse(uint8_t type, data_t * p_advdata, data_t * p_typedata)
{
    uint32_t index = 0;
    uint8_t * p_data;

    p_data = p_advdata->p_data;

    while (index < p_advdata->data_len)
    {
        uint8_t field_length = p_data[index];
        uint8_t field_type = p_data[index+1];

        if (field_type == type)
        {
            p_typedata->p_data = &p_data[index+2];
            p_typedata->data_len = field_length-1;
            return NRF_SUCCESS;
        }
        index += field_length+1;
    }
    return 1;
}

void hm_bt_core_sensing_read_response(uint8_t *data, uint16_t size, uint16_t offset, uint8_t *mac){

  uint8_t id = getBeaconId(mac);

  if(id != 99){
    if(bt_data_handler(data,size,&mBeacons[id]) != true){
      hm_bt_hal_read_data(mac,offset + size);
    }
  }
}

void hm_bt_core_sensing_write_response(uint8_t *mac){
  writeNextJunk(mac);
}

void hm_bt_core_sensing_read_notification(uint8_t *mac){
  hm_bt_hal_read_data(mac,0);
}

void hm_bt_core_sensing_ping_notification(uint8_t *mac){
  #ifdef GARAGE_BLE
        hm_ctw_ping();
  #endif
}

void hm_bt_core_sensing_scan_start(void)
{
    isAuth = false;
}

void hm_bt_core_sensing_discovery_event(uint8_t *mac)
{
    sendGetCertificate(mac);
}

void hm_bt_core_clock(){
	clock ++;
  hm_ctw_ping();
#ifdef GARAGE_BLE
	checkBeacons();
#endif

#ifdef CAN_HACK
if(m_send_hack_async == true && m_is_writing_data == false){

    hm_bt_debug_hal_log("HACK CLOCK");

    hm_bt_hal_delay_ms(2000);

    if(m_send_hack_empty_round == false){
      hm_bt_debug_hal_log("SKIP SEND FAKE ASYNC");
      m_send_hack_empty_round = true;
      return;
    }

    m_send_hack_empty_round = false;
    m_send_hack_async = false;

    uint8_t data[209];
    hm_bt_debug_hal_log("SEND FAKE ASYNC");
    data[0] = 0x02;
    data[1] = m_send_hack_async_type;
    data[2] = 0x00;
    data[3] = 0x00;

    sendSecureContainer(m_hack_p_client.device.serial_number, data, 4);

  }
#endif
}

void hm_bt_ble_on(uint8_t action){
  if(action == 0x01){
    hm_bt_hal_scan_start();
    BLE_ON = 1;
  }else{
    BLE_ON = 0;
    hm_bt_hal_scan_stop();
    reportBeaconLeaveForAll();
  }
}

/**
 * Initializes the SoftDevice and the BLE event interrupt, which dispatches
 * all events to this application.
 */
void hm_bt_core_init(void) {

  //TEST AES CTR
/*
  uint8_t test_nonce[9] = {0x01,0x01,0x03,0x03,0x04,0x02,0x01,0x02,0x04};
  uint8_t test_t_nonce[9] = {0x01,0x02,0x03,0x05,0x05,0x06,0x07,0x08,0x09};
  uint8_t test_key[16] = {0x00,0x01,0x02,0x03,0x05,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0d,0x0e};

  uint8_t test_data[20] = {0x01,0x02,0x03,0x04,0x05,0x01,0x02,0x03,0x04,0x05,0x01,0x02,0x03,0x04,0x05,0x01,0x02,0x03,0x04,0x05};

  hm_bt_debug_hal_log("NONCE");
  hm_bt_debug_hal_log_hex(test_nonce,9);

  hm_bt_debug_hal_log("TRANSACTION NONCE");
  hm_bt_debug_hal_log_hex(test_t_nonce,9);

  hm_bt_debug_hal_log("KEY");
  hm_bt_debug_hal_log_hex(test_key,16);

  hm_bt_debug_hal_log("DATA");
  hm_bt_debug_hal_log_hex(test_data,20);

  hm_bt_core_encrypt_decrypt(test_nonce,test_t_nonce,test_key,test_data,20);

  hm_bt_debug_hal_log("ENCRYPTED DATA");
  hm_bt_debug_hal_log_hex(test_data,20);

  hm_bt_core_encrypt_decrypt(test_nonce,test_t_nonce,test_key,test_data,20);

  hm_bt_debug_hal_log("DECRYPTED DATA");
  hm_bt_debug_hal_log_hex(test_data,20);
*/

  //Init BLE
  initBeaconList();
  initMajorMinorList();

  hm_bt_hal_init();

  hm_bt_hal_scan_start();

  hm_ctw_init();

}

#define BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA 0xFF
#define BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME 0x09
#define BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE 0x07

void hm_bt_core_sensing_process_advertisement(const uint8_t *mac, uint8_t * data, uint8_t size){

  data_t adv_data;
  adv_data.data_len = size;
  adv_data.p_data = data;
  data_t type_data;

  uint16_t major = 0;
  uint16_t minor = 0;

    uint32_t err_code = adv_report_parse(BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA, &adv_data, &type_data);
    if (err_code == NRF_SUCCESS){
      major = type_data.p_data[21] | (type_data.p_data[20] << 8);
      minor = type_data.p_data[23] | (type_data.p_data[22] << 8);
    }

      //IF parrot then we have service uuid in adv package
      uint8_t id = getBeaconId(mac);

      if(id == 99){

        err_code = adv_report_parse(BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME, &adv_data, &type_data);

        uint8_t name[8];

        if (err_code == NRF_SUCCESS){
          memcpy(name,type_data.p_data,8);
        }

        id = getBeaconIdName(name);

        //TODO update beacon mac
        if(id != 99){
          //memcpy(mBeacons[id].device.mac,mac,6);
        }

        if(id == 99){

          err_code = adv_report_parse(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE, &adv_data, &type_data);

          //err_code = adv_report_parse(BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA, &adv_data, &type_data);

          if(err_code == NRF_SUCCESS){

            addMajorMinorToList(mac,major,minor);
            addNameToList(mac,name);

            hm_bt_debug_hal_log("ISSUER DATA FOUND");

            hm_bt_debug_hal_log_hex(type_data.p_data,12);
            hm_bt_debug_hal_log_hex(type_data.p_data + 12,4);

            //Start connect when found
            uint8_t issuer[4];
            hm_conf_access_get_issuer(issuer);

            if(memcmp(type_data.p_data + 12,issuer,4)==0)
            {
              hm_bt_debug_hal_log("HAS VALID ISSUER");

              uint8_t appId[12];
              hm_conf_access_get_app_id(appId);

              if(memcmp(type_data.p_data,appId,12)==0)
              {
                hm_bt_debug_hal_log("HAS VALID APP ID");

                hm_bt_hal_connect(mac);
              }
             }
          }
        }
      }
}

void hm_bt_core_link_connect(uint8_t *mac){
  if(getBeaconId(mac) == 99){
    uint8_t name[8];
    client_handling_create(mac, 0, 0, name, true);
  }
}

void hm_bt_core_link_disconnect(uint8_t *mac){
  uint8_t id = getBeaconId(mac);
  if(id != 99){
    client_handling_destroy(mac);
  }
}

void hm_bt_core_link_incoming_data(uint8_t *data, uint16_t size, uint8_t *mac){
  uint8_t id = getBeaconId(mac);
  if(id != 99){
    bt_data_handler(data, size, &mBeacons[id]);
  }
}
