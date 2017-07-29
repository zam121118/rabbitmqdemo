#  About how I learn to use rabbitmq

## Install rabbitmq-server

[checking this blog](http://www.rabbitmq.com/install-debian.html)

install a rabbitmq-server on my amy@heizi.com host:

`Status of node rabbit@heizi` ( default node name rabbit@+local hostname)

## use web-end to look rabbitmq

use rabbitmq-server + java + spring seeing with this [blog](http://blog.csdn.net/younger_z/article/details/53243990)

```sh
amy@heizi:/usr/lib/rabbitmq/bin$ sudo rabbitmq-plugins enable rabbitmq_management
The following plugins have been enabled:
  amqp_client
  cowlib
  cowboy
  rabbitmq_web_dispatch
  rabbitmq_management_agent
  rabbitmq_management
```

open `http://localhost:15672/` to facilate on web

## Port Access

>SELinux, and similar mechanisms may prevent RabbitMQ from binding to a port. When that happens, RabbitMQ will fail to start.
Firewalls can prevent nodes and CLI tools from communicating with each other. Make sure the following ports can be opened:
>
> - 4369: epmd, a peer discovery service used by RabbitMQ nodes and CLI tools
> - 5672, 5671: used by AMQP 0-9-1 and 1.0 clients without and with TLS
>- 25672: used by Erlang distribution for inter-node and CLI tools communication and is allocated from a dynamic range
>  (limited to a single port by default, computed as AMQP port + 20000). See networking guide for details.
> - 15672: HTTP API clients and rabbitmqadmin (only if the management plugin is enabled)
> - 61613, 61614: STOMP clients without and with TLS (only if the STOMP plugin is enabled)
> - 1883, 8883: (MQTT clients without and with TLS, if the MQTT plugin is enabled
> - 15674: STOMP-over-WebSockets clients (only if the Web STOMP plugin is enabled)
> - 15675: MQTT-over-WebSockets clients (only if the Web MQTT plugin is enabled)
>
> It is possible to configure RabbitMQ to use different ports and specific network interfaces.
> Default user access
>
> The broker creates a user `guest` with password `guest`. Unconfigured clients will in general use these credentials.
> By default, these credentials can only be used when connecting to the broker as localhost so you will need
>  to take action before connecting from any other machine.
>
> See the documentation on access control for information on how to create more users, delete the guest user,
> or allow remote access to the  guest user.

