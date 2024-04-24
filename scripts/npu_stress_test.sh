#! /system/bin/sh

# Function to run the loop
run_loop() {
    DIR=$1
    EXECUTABLE=$2
    MODEL=$3
    IMAGE=$4

    # Check if the directory exists
    if [ -d "$DIR" ]; then
        cd $DIR
        export LD_LIBRARY_PATH=./lib
        while true
        do
            ./$EXECUTABLE $MODEL $IMAGE
        done
        # Return to the original directory
        cd -
    fi
}

# Save the current directory
CURRENT_DIR=$(pwd)

# Run the loops
run_loop "/vendor/bin/rknn_ssd_demo_Android/" "rknn_ssd_demo" "model/RK3588/ssd_inception_v2.rknn" "model/road.bmp"
run_loop "/vendor/bin/rknn_mobilenet_demo_Android/" "rknn_mobilenet_demo" "model/mobilenet_v1.rknn" "model/dog_224x224.jpg"
