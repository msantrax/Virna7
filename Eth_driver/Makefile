#
# Makefile for the Freescale network device drivers.
#


ifeq ($(CONFIG_FEC_MPC52xx_MDIO),y)
	obj-$(CONFIG_FEC_MPC52xx) += virna7_mpc52xx_phy.o
endif

obj-$(CONFIG_GIANFAR) += virna7_driver.o
obj-$(CONFIG_PTP_1588_CLOCK_GIANFAR) += virna7_ptp.o
virna7_driver-objs := virna7.o \
		virna7_ethtool.o \
		virna7_sysfs.o
obj-$(CONFIG_UCC_GETH) += ucc_geth_driver.o
ucc_geth_driver-objs := ucc_geth.o ucc_geth_ethtool.o
