
#ifndef HM_CONFIG_H_
#define HM_CONFIG_H_

//Turns on debug functionality
#define DEBUG
#define MAX_CLIENTS  5
#define MAX_COMMAND_SIZE 10024
#define HM_BT_LOG_LEVEL 3

////H-M BT
#define HM_BT_DEVICE_NAME                   "HM"	                    /**< Name of device. Will be included in the advertising data. */

////H-M Beacon info
#define HM_BT_IBEACON //This will turn on beacon advertisement
#define HM_BT_UPDATE_NAME //This will turn on beacon major and minor change after every create token
#define HM_BT_AUTH_TOKENS 1 //This will tell how many tokens are allowed
#define APP_TX_POWER				  			4								/**< The Tx power for radio, can be of values, accepted values are -40, -30, -20, -16, -12, -8, -4, 0, and 4 dBm */
#define HM_BT_IBEACON_MEASURED_RSSI             0xb8                            /**< The iBeacon's measured RSSI at 1 meter distance in dBm. */
#define HM_BT_IBEACON_MAJOR_VALUE               0x00, 0x01                      /**< Major value used to identify iBeacons. */
#define HM_BT_IBEACON_MINOR_VALUE               0x00, 0x02                      /**< Minor value used to identify iBeacons. */

////H-M TX EX Service configuration parameters
#define HM_BT_TXRX //This will turn on TXRX service

////H-M SPI Config
#define SPI_OPERATING_FREQUENCY (0x02000000uL << (uint32_t)Freq_1Mbps) // Slave clock frequency

// SPI0.
#define SPI_PSELSCK0            9u                                     	// SPI clock GPIO pin number
#define SPI_PSELMOSI0           10u                                     	// SPI Master Out Slave In GPIO pin number
#define SPI_PSELMISO0           11u                                     	// SPI Master In Slave Out GPIO pin number

// SPI1.
#define SPI_PSELSCK1            3u                                     	// SPI clock GPIO pin number
#define SPI_PSELMOSI1           2u                                     	// SPI Master Out Slave In GPIO pin number
#define SPI_PSELMISO1           1u                                     	// SPI Master In Slave Out GPIO pin number

#define SPI_TIMEOUT_COUNTER         0x3000uL                               	// Timeout for SPI transaction in units of loop iterations

#define SPI_TX_BUFFER_SIZE          64u                                		// SPI TX buffer size
#define SPI_RX_BUFFER_SIZE          SPI_TX_BUFFER_SIZE                      	// SPI RX buffer size

// Slave config
#define SPI_SLAVE_MISO_PIN               0u
#define SPI_SLAVE_MOSI_PIN               1u
#define SPI_SLAVE_SCK_PIN                2u
#define SPI_SLAVE_CSN_PIN                3u

///DEBUG
#define HM_DEBUG_TX_PIN_NUMBER               27u
#define HM_DEBUG_RX_PIN_NUMBER               26u

///CRYPTO
#define HM_CRYPTO_SDA              18u
#define HM_CRYPTO_SCL              17u


#endif
