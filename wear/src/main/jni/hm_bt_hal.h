
#include <stdint.h>

uint32_t hm_bt_hal_init();

uint32_t hm_bt_hal_scan_start();
uint32_t hm_bt_hal_scan_stop();

uint32_t hm_bt_hal_advertisement_start();
uint32_t hm_bt_hal_advertisement_stop();

uint32_t hm_bt_hal_connect(const uint8_t *mac);
uint32_t hm_bt_hal_disconnect(uint8_t *mac);

uint32_t hm_bt_hal_service_discovery(uint8_t *mac);

uint32_t hm_bt_hal_write_data(uint8_t *mac, uint16_t length, uint8_t *data);
uint32_t hm_bt_hal_read_data(uint8_t *mac, uint16_t offset);

void hm_bt_hal_delay_ms(uint32_t number_of_ms);
