
#include "hm_bt_debug_hal.h"
#include <stdarg.h>
#include <android/log.h>

void hm_bt_debug_hal_log(const char *str, ...){
  va_list args;
  va_start(args, str);
   __android_log_vprint(ANDROID_LOG_DEBUG, "HM BT Core", str, args);
  va_end(args);
}

void hm_bt_debug_hal_log_hex(const uint8_t *data, const uint16_t length){
   uint16_t i;
   for(i = 0 ; i < length ; i++){
   __android_log_print(ANDROID_LOG_DEBUG, "HM BT Core", "%x",data[i]);
  }
}
