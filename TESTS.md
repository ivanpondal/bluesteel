# Performance & Reliability tests

## Sending/Receiving data

The purpose of this test is to evaluate the different delivery methods provided
by the bluetooth low energy protocol.

### Variables

- Payload size (MTU)
- Delivery method
- Rate limiting
- Background mode
- Connection length
- Concurrent connections
- Physical distance

### Environment

| Device       | OS & Version |
| ------------ | ------------ |
| Iphone 8     | TODO |
| Iphone SE    | TODO |
| Redmi Note 4 | TODO |
| Redmi J ??   | TODO |

### Test data

The data sent will be limited by the minimum MTU (Maximum transmission unit) of
the two devices communicating. The payload itself will be random data plus an
index used to verify the order and delivery of data.

### Metrics

We'll look at different metrics in order to evaluate the effectiveness of each method.

| Metric              | Description                     |
| ------------------- | ------------------------------- |
| Throughput          | Successful packets per minute   |
| Error rate          | Unsuccessful packets per minute |
| Battery consumption | Percentage of battery drain     |
| Signal strength     | Physical signal strength        |
| Network latency     | Time a request and response is travelling in the air |
| Processing time     | Time processing a given request |
| Response time       | Network latency + Processing time |

#### Communication with write & indication ACK

![Bluetooth communication with ACKs](diagrams/bluetooth_communication.png)

### Test scenarios

These are some of the test scenarios we'll be using to measure the performance
of the bluetooth low energy protocol based on how we foresee it'll be used on
our product.

#### Only writes

The client sends a series of numbered messages to the server without expecting a
reply.

The goal is to see how reliable message sending is and how it is affected by the
different variables described earlier.

| Test case | MTU | Throttling | In Background | ACK      | Distance | # Connections | # Messages |
| --------- | --- | ---------- | ------------- | -------- | -------- | ------------- | ---------- |
| OW-1      | Max | None       | None          | Yes      | 1 meter  | 1             | 100        |


#### Only indications

#### Ping pong - Writes & indications

#### Parallell - Writes & indications

#### Subscription - Read & indications