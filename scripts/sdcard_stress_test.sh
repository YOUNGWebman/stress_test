#! /system/bin/sh

#for andriod, default mount and output path
OUT_PATH=/mnt/ex_sd

#single file size, BLOCK_SIZE * BLOCK_COUNT (Byte)
BLOCK_SIZE=512
BLOCK_COUNT=102400
#file count, auto calculation
FILE_COUNT=0
#emmc avail size, auto claculation
AVAIL_SIZE=0

setprop rp.sdcard.storage.flag false
setprop rp.sdcard.storage.total 0
setprop rp.sdcard.storage.used 0
setprop rp.sdcard.rw.count 0

count=$(getprop my.script.execution.count)

cd /sys/class/mmc_host/

detect_flag=0

for mmc in `ls -d mmc*`; do

    [ ! -d ${mmc}/$mmc\:* ] && continue
    mmc_type=`cat $mmc/$mmc\:*/type` 
    if [ "$mmc_type" == "SD" ];then
        detect_flag=1
        echo "Detect SD card!!"
        echo =============================
        echo SD card info
        echo =============================
        cat /sys/kernel/debug/$mmc/ios
        
        setprop rp.sdcard.storage.flag true
        
        
        mountpoint=/mnt/ex_sd
        umount $mountpoint
        
        #get sdcard mmc channl, and get mount info, such as: df -h | hrep mmcblk1
        mountinfo=`df -h |grep mmcblk${mmc: -1}`
        if [ "$mountinfo" == "" ];then
	    echo "SD card not auto mount! manual mounting..."

            mountpoint=/mnt/ex_sd
            umount $mountpoint
            mkdir $mountpoint
            [ ! -d $mountpint ] && mkdir $mountpoint
            if [ -e /dev/block/mmcblk${mmc: -1}p1 ]; then 
                mount /dev/block/mmcblk${mmc: -1}p1 $mountpoint
            elif [ -e /dev/block/mmcblk${mmc: -1} ];then
                mount /dev/block/mmcblk${mmc: -1}p1 $mountpoint
            else
                echo "SD card not found available partiniton! exit!!"
            fi
            df -h
        else
            mountpoint=$(echo $mountinfo | awk '{print $6}')
            echo "SD card mount point: " $mountpoint
        fi
   fi


done

sdcard_flag=$(getprop rp.sdcard.storage.flag)

if [ "$sdcard_flag" == "false"]; then
	setprop rp.sdcard.rw.count no_sdcard
	exit 11
fi


function get_emmc_avail_size()
{
	[  -d $OUT_PATH/rp_stress_path ]
  rm -Rf $OUT_PATH/rp_stress_path
	#for andriod, always output to /data
	AVAIL_SIZE=$(df | grep -w "$OUT_PATH" | awk '{print $4}')

	#for linux, if root > part, use root, otherwise use part
	if [ "X$AVAIL_SIZE" == "X" ];then
		
		root_size=$(df | grep -w "/" | awk '{print $4}')
		part_size=$(df | grep -w "mmcblk" | awk '{print $4}')

		if [ "X$part_size" == "X" ];then
			AVAIL_SIZE=$root_size
			return
		fi

		if [ $root_size -lt $part_size ];then
			OUT_PATH=$(df | grep -w "mmcblk" | awk '{print $6}')
			AVAIL_SIZE=$part_size
			echo "change OUT_PATH to $OUT_PATH"
		fi
	fi
}


function write_read_file()
{
	FILE_SIZE=$(($BLOCK_SIZE * $BLOCK_COUNT))
# Byte for MB
	FILE_SIZE=$(($FILE_SIZE / 1024 /1024))
# KB for MB
	AVAIL_SIZE=$(($AVAIL_SIZE / 1024))
	FILE_COUNT=$(((($AVAIL_SIZE / $FILE_SIZE) * 9) / 10))
    setprop my.script.execution.count 0
	[ ! -d $OUT_PATH/rp_stress_path ] && mkdir -p $OUT_PATH/rp_stress_path

	echo "AVAIL_SIZE: ${AVAIL_SIZE}M"
	echo "FILE_SIZE: ${FILE_SIZE}M"
	echo "FILE_COUNT: ${FILE_COUNT}"
	echo "OUT_PATH: ${OUT_PATH}"
	
	total=`df  $OUT_PATH | tail -1 | awk '{print $2 }' | cut -d'%' -f1`
	echo $total > /dev/tty
	setprop rp.sdcard.storage.total $total
	
	while [ 1 ];
	do
		loop=0
		while [ ${loop} -lt $FILE_COUNT ];
		do
		        for i in $(seq 1 10); do
		                echo "dd if=/dev/zero of=$OUT_PATH/rp_stress_path/file${loop} bs=${BLOCK_SIZE} count=$BLOCK_COUNT"
		                dd if=/dev/zero of=$OUT_PATH/rp_stress_path/file${loop} bs=${BLOCK_SIZE} count=$BLOCK_COUNT
		                sync && echo 3 > /proc/sys/vm/drop_caches
		                
		                FILE=$OUT_PATH/rp_stress_path/file${loop}
		                if [ -f "$FILE" ];then
		                	dd if=/$OUT_PATH/rp_stress_path/file${loop} of=/dev/zero
		                else
		                	setprop rp.sdcard.rw.count failed
		                	echo "failed" > /dev/tty
		                	exit 1111
		                fi
		                
		                dd if=/$OUT_PATH/rp_stress_path/file${loop} of=/dev/zero
		                sync && echo 3 > /proc/sys/vm/drop_caches
		                let loop=${loop}+1
		                
						        used=`df  $OUT_PATH | tail -1 | awk '{print $3 }' | cut -d'%' -f1`
						        setprop rp.sdcard.storage.used $used
		        done
		done
        # 增加计数值
        count=$((count + 1))
       # 设置新的计数值
       setprop rp.sdcard.rw.count $count
        rm -rf $OUT_PATH/rp_stress_path/*
	done
}

get_emmc_avail_size
write_read_file


cd -

umount $mountpoint
rm -Rf $mountpoint

[ "$detect_flag" == "0" ] && echo "SD card not found!"

