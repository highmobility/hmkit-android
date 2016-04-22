
#include "hm_ctw_customer.h"
#include "hm_cert.h"
#include <string.h>
#include "hm_config.h"

void hm_ctw_init()
{

}

void hm_ctw_ping()
{

}

void hm_ctw_authorised_device_added(hm_device_t *device, uint8_t error)
{

}

void hm_ctw_authorised_device_updated(hm_device_t *device, uint8_t error)
{

}

void hm_ctw_entered_proximity(hm_device_t *device)
{
    //TODO add also app id to device
}

void hm_ctw_proximity_measured(hm_device_t *device, uint8_t receiver_count, hm_receiver_t *receivers)
{

}

void hm_ctw_exited_proximity(hm_device_t *device)
{

}

void hm_ctw_command_received(hm_device_t *device, uint8_t *data, uint8_t *length, uint8_t *error)
{

}

uint32_t hm_ctw_get_device_certificate_failed(hm_device_t *device, uint8_t *nonce)
{
    return 0;
}

void hm_ctw_device_certificate_registered(hm_device_t *device, uint8_t *public_key, uint8_t error)
{

}

uint32_t hm_ctw_pairing_requested(){

    return 0;
}