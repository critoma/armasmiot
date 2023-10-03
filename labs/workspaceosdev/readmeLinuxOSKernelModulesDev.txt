# https://sysprog21.github.io/lkmpg/
# optional: https://www.thegeekstuff.com/2013/06/compile-linux-kernel/
# https://www.thegeekstuff.com/2013/07/write-linux-kernel-module/
# https://us-east-2.console.aws.amazon.com/ec2/home?region=us-east-2#Instances:
# critoma@critoma-mac Downloads % ssh -i "clusterAwsEc2_01.pem" ubuntu@ec2-3-149-28-62.us-east-2.compute.amazonaws.com


# list Linux OS Kernel modules:
lsmod

# in Ubuntu 22 from Amazon EC2 Cloud, they are partailly already installed - https://shell.cloud.google.com/?show=ide%2Cterminal&authuser=3&fromcloudshell=true
# sudo apt-get update
# sudo apt search linux-headers
# uname -r
# sudo apt-get install build-essential linux-headers-$(uname -r)
# sudo apt-get install build-essential linux-headers-5.19.0-1024-aws
# in RPi3: sudo apt-get install dkms build-essential && sudo apt-get install raspberrypi-kernel-headers


mkdir -p ~/embedOS/hello-module
cd ~/embedOS/hello-module
touch hello.c

# insert this code in C and compile it with make:
######################
#include <linux/module.h>    // included for all kernel modules
#include <linux/kernel.h>    // included for KERN_INFO
#include <linux/init.h>      // included for __init and __exit macros

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Cristian Toma");
MODULE_DESCRIPTION("A Simple Hello World module");

static int __init hello_init(void)
{
    printk(KERN_INFO "Hello world!\n");
    return 0;    // Non-zero return means that the module couldn't be loaded.
}

static void __exit hello_cleanup(void)
{
    printk(KERN_INFO "Cleaning up module.\n");
}

module_init(hello_init);
module_exit(hello_cleanup);
######################

touch Makefile
nano Makefile

################# insert this into Makefile (make sure you have tabs and not spaces):
obj-m += hello.o

PWD := $(CURDIR)

all:
    make -C /lib/modules/$(shell uname -r)/build M=$(PWD) modules

clean:
    make -C /lib/modules/$(shell uname -r)/build M=$(PWD) clean
#################

make

# sudo insmod hello.ko

# sudo dmesg | tail -1

# lsmod | grep hello

# sudo rmmod hello.ko

# sudo dmesg | tail -1
# sudo dmesg | tail -2

### Kernel OS Modules for crypto:
cd ..
mkdir sha256-module
cd sha256-module
touch cryptosha256.c
nano cryptosha256.c

#################################################
#include <crypto/internal/hash.h> 
#include <linux/module.h> 
 
#define SHA256_LENGTH 32 
 
static void show_hash_result(char *plaintext, char *hash_sha256) 
{ 
    int i; 
    char str[SHA256_LENGTH * 2 + 1]; 
 
    pr_info("sha256 test for string: \"%s\"\n", plaintext); 
    for (i = 0; i < SHA256_LENGTH; i++) 
        sprintf(&str[i * 2], "%02x", (unsigned char)hash_sha256[i]); 
    str[i * 2] = 0; 
    pr_info("%s\n", str); 
} 
 
static int __init cryptosha256_init(void) 
{ 
    char *plaintext = "This is a test"; 
    char hash_sha256[SHA256_LENGTH]; 
    struct crypto_shash *sha256; 
    struct shash_desc *shash; 
 
    sha256 = crypto_alloc_shash("sha256", 0, 0); 
    if (IS_ERR(sha256)) { 
        pr_err( 
            "%s(): Failed to allocate sha256 algorithm, enable CONFIG_CRYPTO_SHA256 and try again.\n", 
            __func__); 
        return -1; 
    } 
 
    shash = kmalloc(sizeof(struct shash_desc) + crypto_shash_descsize(sha256), 
                    GFP_KERNEL); 
    if (!shash) 
        return -ENOMEM; 
 
    shash->tfm = sha256; 
 
    if (crypto_shash_init(shash)) 
        return -1; 
 
    if (crypto_shash_update(shash, plaintext, strlen(plaintext))) 
        return -1; 
 
    if (crypto_shash_final(shash, hash_sha256)) 
        return -1; 
 
    kfree(shash); 
    crypto_free_shash(sha256); 
 
    show_hash_result(plaintext, hash_sha256); 
 
    return 0; 
} 
 
static void __exit cryptosha256_exit(void) 
{ 
} 
 
module_init(cryptosha256_init); 
module_exit(cryptosha256_exit); 
 
MODULE_DESCRIPTION("sha256 hash test"); 
MODULE_LICENSE("GPL");
#################################################

cp ../hello-module/Makefile .
# modify Makefile (hint: first line with cryptosha256 instead of hello)
make

# sudo insmod cryptosha256.ko

# sudo dmesg | tail -1

# lsmod | grep cryptosha256

# sudo rmmod cryptosha256.ko

# sudo dmesg | tail -1
# sudo dmesg | tail -2

# https://sysprog21.github.io/lkmpg/#interrupt-handlers
