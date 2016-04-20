
#include "hm_bt_debug_hal.h"
#include <stdarg.h>

void hm_bt_debug_hal_log(const char *str, ...){
  va_list args;
  va_start(args, str);
  //debug_log_args(str, args);
  va_end(args);
}

void hm_bt_debug_hal_log_hex(const uint8_t *data, const uint16_t length){
  //debug_log_hex(data, length);
}
