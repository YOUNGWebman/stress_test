#! /system/bin/sh

sleep 5

wlan_status=`wpa_cli -i wlan0 status`

tmp=${wlan_status#*wpa_state=}
echo "wpa_state  : $tmp"
wpa_state=${tmp:0:9}
echo "WIFI status is : $wpa_state"
if [ "$wpa_state" == "DISCONNEC" ]; then
	echo "wifi open but no connected"
elif [ "$wpa_state" == "COMPLETED" ]; then
	echo "wifi open connect success"
else
	echo "wifi is closed"
fi

echo
while [ true ]; 
do	
ping  -I wlan0 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "wifi ping success"
	setprop rp_wifi_state success
else
	echo "wifi ping failed"
	setprop rp_wifi_state failed
fi

echo

sleep 1

ping  -I eth0 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "eth0 ping success"
	setprop rp_eth0_state success
else
	echo "eth0 ping failed"
	setprop rp_eth0_state failed
fi

echo

sleep 1

ping  -I eth1 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "eth1 ping success"
	setprop rp_eth1_state success
else
	echo "eth1 ping failed"
	setprop rp_eth1_state failed
fi

echo

sleep 1

ping  -I eth2 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "eth2 ping success"
	setprop rp_eth2_state success
else
	echo "eth2 ping failed"
	setprop rp_eth2_state failed
fi
echo

sleep 1

ping  -I wwan0 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "wwan0 ping success"
	setprop rp_wwan0_state success
else
	echo "wwan0 ping failed"
	setprop rp_wwan0_state failed
fi

echo

sleep 1

ping  -I usb0 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "usb0 ping success"
	setprop rp_usb0_state success
else
	echo "usb0 ping failed"
	setprop rp_usb0_state failed
fi

echo

sleep 1

ping  -I ppp0 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "ppp0 ping success"
	setprop rp_ppp0_state success
else
	echo "ppp0 ping failed"
	setprop rp_ppp0_state failed
fi

echo

sleep 1

ping  -I rmnet_mhi0.1 -c 1 www.baidu.com
ping_result=$?
echo "ping_result $ping_result"
if [ "$ping_result" == "0" ]; then
	echo "rmnet_mhi0.1 ping success"
	setprop rp_rmnet_state success
else
	echo "rmnet_mhi0.1 ping failed"
	setprop rp_rmnet_state failed
fi
echo

wifi_flag=$(getprop rp_wifi_state)
eth0_flag=$(getprop rp_eth0_state)
eth1_flag=$(getprop rp_eth1_state)
eth2_flag=$(getprop rp_eth2_state)
wwan0_flag=$(getprop rp_wwan0_state)
usb0_flag=$(getprop rp_usb0_state)
rmnet_flag=$(getprop rp_rmnet_state)

done
