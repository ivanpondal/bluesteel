# SR-OW-1

## Setup

My measurements were based by counting steps. For me, that's 1 step ~= 77.5 cm.

In order to split test files the following command was used:
```bash
split -d -p"Pasos" src_<SRC_DEVICE_ID>.txt dst_<DST_DEVICE_ID>_
```

To get the values in milliseconds of each write, the following command was used:
```bash
cat src-<SRC_DEVICE_ID>/dst_<DST_DEVICE_ID>_XX | grep write | cut -d' ' -f8
```

## Results

### Iphone SE -> Iphone 8

| Distance (m) | min | q1  | median | q3   | max | sum  | mean   | stddev  | stderr   | variance |
| ------------ | --- | --- | ------ | ---- | --- | ---- | ------ | ------- | -------- | -------- |
| 3.1          | 54  | 57  | 58     | 87   | 179 | 7939 | 79.39  | 34.735  | 3.4735   | 1206.52  |
| 6.2          | 55  | 57  | 58     | 87   | 177 | 7310 | 73.1   | 25.3921 | 2.53921  | 644.758  |
| 12.4         | 55  | 57  | 58.5   | 87   | 271 | 8468 | 84.68  | 40.7448 | 4.07448  | 1660.14  |
| 24.8         | 56  | 57  | 87     | 117  | 282 | 10064| 100.64 | 53.0613 | 5.30613  | 2815.51  |
| 37.2         | 55  | 57  | 87     | 118  | 291 | 10503| 105.03 | 53.5689 | 5.35689  | 2869.63  |
| 49.6         | 56  | 57  | 86     | 116  | 257 | 9232 | 92.32  | 40.2823 | 4.02823  | 1622.66  |
| 62           | 56  | 58  | 87     | 117.5| 631 | 10413| 104.13 | 64.2847 | 6.42847  | 4132.52  |
| 78           | 56  | 57  | 87     | 117  | 807 | 12503| 125.03 | 135.367 | 13.5367  | 18324.4  |

### Iphone 8 -> Iphone SE

| Distance (m) | min | q1  | median | q3   | max | sum  | mean   | stddev  | stderr   | variance |
| ------------ | --- | --- | ------ | ---- | --- | ---- | ------ | ------- | -------- | -------- |
| 3.1          | 52  | 58  | 59     | 89   | 181 | 7633 | 76.33  | 29.5488 | 2.95488  | 873.132  |
| 6.2          | 57  | 58  | 58     | 59   | 178 | 6872 | 68.72  | 24.0853 | 2.40853  | 580.103  |
| 12.4         | 57  | 88  | 118    | 178  | 388 | 13746| 137.46 | 71.42   | 7.142    | 5100.82  |
| 24.8         | 57  | 58  | 58     | 88   | 230 | 7590 | 75.9   | 33.4188 | 3.34188  | 1116.82  |
| 49.6         | 57  | 58  | 88     | 148  | 269 | 10935| 109.35 | 53.5317 | 5.35317  | 2865.64  |
| 78           | 57  | 88  | 148    | 208  | 754 | 16078| 160.78 | 99.5552 | 9.95552  | 9911.24  |

## Conclusions

I was able to reach up to 78 meters before losing connection. During these runs,
speed was approximately 2kbytes/sec, this only lowered to 1kbyte/sec when at
maximum range.
