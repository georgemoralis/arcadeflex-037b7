/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
//drivers imports
import static arcadeflex.v037b7.drivers.wc90.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.cpuintrfH.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.palette.*;
//sound imports
import static arcadeflex.v037b7.sound._2203intf.*;
import static arcadeflex.v037b7.sound._2203intfH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.wc90b.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class wc90b {

    //#define TEST_DIPS false /* enable to test unmapped dip switches */
    public static WriteHandlerPtr wc90b_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            bankaddress = 0x10000 + ((data & 0xf8) << 8);
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));
        }
    };

    public static WriteHandlerPtr wc90b_bankswitch1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU2);

            bankaddress = 0x10000 + ((data & 0xf8) << 8);
            cpu_setbank(2, new UBytePtr(RAM, bankaddress));
        }
    };

    public static WriteHandlerPtr wc90b_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(2,/*Z80_NMI_INT*/ -1000);
        }
    };

    static MemoryReadAddress wc90b_readmem1[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x9fff, MRA_RAM), /* Main RAM */
                new MemoryReadAddress(0xa000, 0xa7ff, wc90b_tile_colorram_r), /* bg 1 color ram */
                new MemoryReadAddress(0xa800, 0xafff, wc90b_tile_videoram_r), /* bg 1 tile ram */
                new MemoryReadAddress(0xc000, 0xc7ff, wc90b_tile_colorram2_r), /* bg 2 color ram */
                new MemoryReadAddress(0xc800, 0xcfff, wc90b_tile_videoram2_r), /* bg 2 tile ram */
                new MemoryReadAddress(0xe000, 0xe7ff, colorram_r), /* fg color ram */
                new MemoryReadAddress(0xe800, 0xefff, videoram_r), /* fg tile ram */
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_BANK1),
                new MemoryReadAddress(0xf800, 0xfbff, wc90b_shared_r),
                new MemoryReadAddress(0xfd00, 0xfd00, input_port_0_r), /* Stick 1, Coin 1  Start 1 */
                new MemoryReadAddress(0xfd02, 0xfd02, input_port_1_r), /* Stick 2, Coin 2  Start 2 */
                new MemoryReadAddress(0xfd06, 0xfd06, input_port_2_r), /* DIP Switch A */
                new MemoryReadAddress(0xfd08, 0xfd08, input_port_3_r), /* DIP Switch B */
                new MemoryReadAddress(0xfd00, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress wc90b_readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc1ff, MRA_RAM),
                new MemoryReadAddress(0xc200, 0xe1ff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe7ff, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_BANK2),
                new MemoryReadAddress(0xf800, 0xfbff, wc90b_shared_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress wc90b_writemem1[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x8075, MWA_RAM),
                new MemoryWriteAddress(0x8076, 0x8076, MWA_RAM, wc90b_scroll1xlo),
                new MemoryWriteAddress(0x8077, 0x8077, MWA_RAM, wc90b_scroll1xhi),
                new MemoryWriteAddress(0x8078, 0x8078, MWA_RAM, wc90b_scroll1ylo),
                new MemoryWriteAddress(0x8079, 0x8079, MWA_RAM, wc90b_scroll1yhi),
                new MemoryWriteAddress(0x807a, 0x807a, MWA_RAM, wc90b_scroll2xlo),
                new MemoryWriteAddress(0x807b, 0x807b, MWA_RAM, wc90b_scroll2xhi),
                new MemoryWriteAddress(0x807c, 0x807c, MWA_RAM, wc90b_scroll2ylo),
                new MemoryWriteAddress(0x807d, 0x807d, MWA_RAM, wc90b_scroll2yhi),
                new MemoryWriteAddress(0x807e, 0x9fff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa7ff, wc90b_tile_colorram_w, wc90b_tile_colorram),
                new MemoryWriteAddress(0xa800, 0xafff, wc90b_tile_videoram_w, wc90b_tile_videoram, wc90b_tile_videoram_size),
                new MemoryWriteAddress(0xc000, 0xc7ff, wc90b_tile_colorram2_w, wc90b_tile_colorram2),
                new MemoryWriteAddress(0xc800, 0xcfff, wc90b_tile_videoram2_w, wc90b_tile_videoram2, wc90b_tile_videoram_size2),
                new MemoryWriteAddress(0xe000, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe800, 0xefff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xfbff, wc90b_shared_w, wc90b_shared),
                new MemoryWriteAddress(0xfc00, 0xfc00, wc90b_bankswitch_w),
                new MemoryWriteAddress(0xfd00, 0xfd00, wc90b_sound_command_w),
                /*  */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress wc90b_writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe000, 0xe7ff, paletteram_xxxxBBBBGGGGRRRR_swap_w, paletteram),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xfbff, wc90b_shared_w),
                new MemoryWriteAddress(0xfc00, 0xfc00, wc90b_bankswitch1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xe800, 0xe800, YM2203_status_port_0_r),
                new MemoryReadAddress(0xe801, 0xe801, YM2203_read_port_0_r),
                new MemoryReadAddress(0xec00, 0xec00, YM2203_status_port_1_r),
                new MemoryReadAddress(0xec01, 0xec01, YM2203_read_port_1_r),
                new MemoryReadAddress(0xf800, 0xf800, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xe800, 0xe800, YM2203_control_port_0_w),
                new MemoryWriteAddress(0xe801, 0xe801, YM2203_write_port_0_w),
                new MemoryWriteAddress(0xec00, 0xec00, YM2203_control_port_1_w),
                new MemoryWriteAddress(0xec01, 0xec01, YM2203_write_port_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_wc90b = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 bit 0-5 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 bit 0-5 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* DSWA */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "10 Coins/1 Credit");
            PORT_DIPSETTING(0x08, DEF_STR("9C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("8C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("7C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x30, "Easy");
            PORT_DIPSETTING(0x10, "Normal");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, "Continue Game countdown speed");
            PORT_DIPSETTING(0x40, "Normal (1 sec per number)");
            PORT_DIPSETTING(0x00, "Faster (56/60 per number)");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSWB */
            PORT_DIPNAME(0x03, 0x03, "1 Player Game Time");
            PORT_DIPSETTING(0x01, "1:00");
            PORT_DIPSETTING(0x02, "1:30");
            PORT_DIPSETTING(0x03, "2:00");
            PORT_DIPSETTING(0x00, "2:30");
            PORT_DIPNAME(0x1c, 0x1c, "2 Player Game Time");
            PORT_DIPSETTING(0x0c, "1:00");
            PORT_DIPSETTING(0x14, "1:30");
            PORT_DIPSETTING(0x04, "2:00");
            PORT_DIPSETTING(0x18, "2:30");
            PORT_DIPSETTING(0x1c, "3:00");
            PORT_DIPSETTING(0x08, "3:30");
            PORT_DIPSETTING(0x10, "4:00");
            PORT_DIPSETTING(0x00, "5:00");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x80, "Japanese");
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 0x4000 * 8, 0x8000 * 8, 0xc000 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 characters */
            256, /* 256 characters */
            4, /* 4 bits per pixel */
            new int[]{0 * 0x20000 * 8, 1 * 0x20000 * 8, 2 * 0x20000 * 8, 3 * 0x20000 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                (0x1000 * 8) + 0, (0x1000 * 8) + 1, (0x1000 * 8) + 2, (0x1000 * 8) + 3, (0x1000 * 8) + 4, (0x1000 * 8) + 5, (0x1000 * 8) + 6, (0x1000 * 8) + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                0x800 * 8, 0x800 * 8 + 1 * 8, 0x800 * 8 + 2 * 8, 0x800 * 8 + 3 * 8, 0x800 * 8 + 4 * 8, 0x800 * 8 + 5 * 8, 0x800 * 8 + 6 * 8, 0x800 * 8 + 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 32*32 characters */
            4096, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{3 * 0x20000 * 8, 2 * 0x20000 * 8, 1 * 0x20000 * 8, 0 * 0x20000 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                (16 * 8) + 0, (16 * 8) + 1, (16 * 8) + 2, (16 * 8) + 3, (16 * 8) + 4, (16 * 8) + 5, (16 * 8) + 6, (16 * 8) + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 8 * 8 + 1 * 8, 8 * 8 + 2 * 8, 8 * 8 + 3 * 8, 8 * 8 + 4 * 8, 8 * 8 + 5 * 8, 8 * 8 + 6 * 8, 8 * 8 + 7 * 8},
            32 * 8 /* every char takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 1 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x02000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x04000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x06000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x08000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x0a000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x0c000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x0e000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x10000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x12000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x14000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x16000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x1a000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x1c000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x1e000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, spritelayout, 0 * 16 * 16, 16 * 16), // sprites
                new GfxDecodeInfo(-1) /* end of array */};

    /* handler called by the 2203 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            cpu_set_nmi_line(2, irq != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    static YM2203interface ym2203_interface = new YM2203interface(
            2, /* 2 chips */
            2000000, /* 2 MHz ????? */
            new int[]{YM2203_VOL(25, 25), YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteYmHandlerPtr[]{irqhandler, null}
    );

    static MachineDriver machine_driver_wc90b = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6.0 MHz ??? */
                        wc90b_readmem1, wc90b_writemem1, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6.0 MHz ??? */
                        wc90b_readmem2, wc90b_writemem2, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ???? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* NMIs are triggered by the YM2203 */
                /* IRQs are triggered by the main CPU */
                )

            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            4 * 16 * 16, 4 * 16 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            wc90b_vh_start,
            wc90b_vh_stop,
            wc90b_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                )
            }
    );

    static RomLoadPtr rom_wc90b = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 128k for code */
            ROM_LOAD("a02.bin", 0x00000, 0x10000, 0x192a03dd);/* c000-ffff is not used */
            ROM_LOAD("a03.bin", 0x10000, 0x10000, 0xf54ff17a);/* banked at f000-f7ff */

            ROM_REGION(0x20000, REGION_CPU2);/* 96k for code */ /* Second CPU */
            ROM_LOAD("a04.bin", 0x00000, 0x10000, 0x3d535e2f);/* c000-ffff is not used */
            ROM_LOAD("a05.bin", 0x10000, 0x10000, 0x9e421c4b);/* banked at f000-f7ff */

            ROM_REGION(0x10000, REGION_CPU3);/* 192k for the audio CPU */
            ROM_LOAD("a01.bin", 0x00000, 0x10000, 0x3d317622);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a06.bin", 0x000000, 0x04000, 0x3b5387b7);
            ROM_LOAD("a08.bin", 0x004000, 0x04000, 0xc622a5a3);
            ROM_LOAD("a10.bin", 0x008000, 0x04000, 0x0923d9f6);
            ROM_LOAD("a20.bin", 0x00c000, 0x04000, 0xb8dec83e);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a07.bin", 0x000000, 0x20000, 0x38c31817);
            ROM_LOAD("a09.bin", 0x020000, 0x20000, 0x32e39e29);
            ROM_LOAD("a11.bin", 0x040000, 0x20000, 0x5ccec796);
            ROM_LOAD("a21.bin", 0x060000, 0x20000, 0x0c54a091);

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("146_a12.bin", 0x000000, 0x10000, 0xd5a60096);
            ROM_LOAD("147_a13.bin", 0x010000, 0x10000, 0x36bbf467);
            ROM_LOAD("148_a14.bin", 0x020000, 0x10000, 0x26371c18);
            ROM_LOAD("149_a15.bin", 0x030000, 0x10000, 0x75aa9b86);
            ROM_LOAD("150_a16.bin", 0x040000, 0x10000, 0x0da825f9);
            ROM_LOAD("151_a17.bin", 0x050000, 0x10000, 0x228429d8);
            ROM_LOAD("152_a18.bin", 0x060000, 0x10000, 0x516b6c09);
            ROM_LOAD("153_a19.bin", 0x070000, 0x10000, 0xf36390a9);
            ROM_END();
        }
    };

    public static InitDriverPtr init_wc90b = new InitDriverPtr() {
        public void handler() {
            int i;

            /* sprite graphics are inverted */
            for (i = 0; i < memory_region_length(REGION_GFX3); i++) {
                memory_region(REGION_GFX3).write(i, memory_region(REGION_GFX3).read(i) ^ 0xff);
            }
        }
    };

    public static GameDriver driver_wc90b = new GameDriver("1989", "wc90b", "wc90b.java", rom_wc90b, driver_wc90, machine_driver_wc90b, input_ports_wc90b, init_wc90b, ROT0, "bootleg", "Euro League", GAME_NO_COCKTAIL);
}
