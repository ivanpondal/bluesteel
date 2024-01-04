# Dev hardware setup

## nERF52840 MDK USB Dongle

### Setting up sniffer

1. [Download Nordic sniffer (supports ble 5.0)](https://www.nordicsemi.com/Products/Development-tools/nrf-sniffer-for-bluetooth-le/download#infotabs)
2. Convert firmware to UF2 format:
    1. Clone [uf2utils](https://github.com/makerdiary/uf2utils)
    2. Convert dongle firmware:
    ```
    python3 uf2conv/uf2conv.py sniffer_nrf52840dongle_nrf52840_4.1.1.hex --family 0xADA52840 --convert --output firmware.uf2
    ```
    3. Connect dongle pressing button, release when led goes green.
    4. Copy firmware.uf2 to MDK volume. In order to flash other programs, double
       tap button while connected.

## Android

### Enabling HCI bluetooth logs

1. Go to `Settings -> Developer Options`.
2. Enable `Bluetooth HCI snoop log`.
3. Turn off bluetooth and reset device.
4. Run test.
5. Retrieve logs:
    1. Bug report:
    ```
    adb -s <device-id> bugreport <file-name>
    ```
    2. Pulling log from device:
    ```
    adb -s <device-id> pull /sdcard/btsnoop_hci.log
    ```
