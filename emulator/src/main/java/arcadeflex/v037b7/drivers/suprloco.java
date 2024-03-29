/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
//cpu imports
import static arcadeflex.v037b7.cpu.z80.z80H.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//machine imports
import static arcadeflex.v037b7.machine.segacrpt.*;
//mame imports
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.inputH.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
import static arcadeflex.v056.mame.timerH.*;
//sound imports
import static arcadeflex.v058.sound.sn76496.*;
import static arcadeflex.v058.sound.sn76496H.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.suprloco.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class suprloco {

    public static WriteHandlerPtr suprloco_soundport_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_cause_interrupt(1, Z80_NMI_INT);
            /* spin for a while to let the Z80 read the command (fixes hanging sound in Regulus) */
            cpu_spinuntil_time(TIME_IN_USEC(50));
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc1ff, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xc800, input_port_0_r),
                new MemoryReadAddress(0xd000, 0xd000, input_port_1_r),
                new MemoryReadAddress(0xd800, 0xd800, input_port_2_r),
                new MemoryReadAddress(0xe000, 0xe000, input_port_3_r),
                new MemoryReadAddress(0xe001, 0xe001, input_port_4_r),
                new MemoryReadAddress(0xe801, 0xe801, suprloco_control_r),
                new MemoryReadAddress(0xf000, 0xf6ff, MRA_RAM),
                new MemoryReadAddress(0xf7e0, 0xf7ff, suprloco_scrollram_r),
                new MemoryReadAddress(0xf800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc1ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe800, 0xe800, suprloco_soundport_w),
                new MemoryWriteAddress(0xe801, 0xe801, suprloco_control_w),
                new MemoryWriteAddress(0xf000, 0xf6ff, suprloco_videoram_w, suprloco_videoram),
                new MemoryWriteAddress(0xf7e0, 0xf7ff, suprloco_scrollram_w),
                new MemoryWriteAddress(0xf800, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa003, SN76496_0_w),
                new MemoryWriteAddress(0xc000, 0xc003, SN76496_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_suprloco = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x38, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x38, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x28, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xc0, 0x40, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x40, "3");
            PORT_DIPSETTING(0x80, "4");
            PORT_DIPSETTING(0xc0, "5");

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000");
            PORT_DIPSETTING(0x01, "30000");
            PORT_DIPSETTING(0x02, "40000");
            PORT_DIPSETTING(0x03, "50000");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unused"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_BITX(0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, "Initial Entry");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8 by 8 */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1024 * 8 * 8, 2 * 1024 * 8 * 8, 3 * 1024 * 8 * 8}, /* plane */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                /* sprites use colors 256-511 + 512-767 */
                new GfxDecodeInfo(REGION_GFX1, 0x6000, charlayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static SN76496interface sn76496_interface = new SN76496interface(
            2, /* 2 chips */
            new int[]{4000000, 2000000}, /* 8 MHz / 4 ?*/
            new int[]{100, 100}
    );

    static MachineDriver machine_driver_suprloco = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz (?) */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000,
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4 /* NMIs are caused by the main CPU */
                ),},
            60, 5000, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, /* screen_width, screen_height */
            new rectangle(1 * 8, 31 * 8 - 1, 0 * 8, 28 * 8 - 1), /* struct rectangle visible_area */
            gfxdecodeinfo, /* GfxDecodeInfo */
            768, 768,
            suprloco_vh_convert_color_prom, /* convert color prom routine */
            VIDEO_TYPE_RASTER,
            null, /* vh_init routine */
            suprloco_vh_start, /* vh_start routine */
            null, /* vh_stop routine */
            suprloco_vh_screenrefresh, /* vh_update routine */
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
                )
            }
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_suprloco = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);/* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("ic37.bin", 0x0000, 0x4000, 0x57f514dd);/* encrypted */
            ROM_LOAD("ic15.bin", 0x4000, 0x4000, 0x5a1d2fb0);/* encrypted */
            ROM_LOAD("ic28.bin", 0x8000, 0x4000, 0xa597828a);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound cpu */
            ROM_LOAD("ic64.bin", 0x0000, 0x2000, 0x0aa57207);

            ROM_REGION(0xe000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic63.bin", 0x0000, 0x2000, 0xe571fe81);
            ROM_LOAD("ic62.bin", 0x2000, 0x2000, 0x6130f93c);
            ROM_LOAD("ic61.bin", 0x4000, 0x2000, 0x3b03004e);
            /*0x6000- 0xe000 will be created by init_suprloco */

            ROM_REGION(0x8000, REGION_GFX2);/* 32k for sprites data used at runtime */
            ROM_LOAD("ic55.bin", 0x0000, 0x4000, 0xee2d3ed3);
            ROM_LOAD("ic56.bin", 0x4000, 0x2000, 0xf04a4b50);
            /*0x6000 empty */

            ROM_REGION(0x0620, REGION_PROMS);
            ROM_LOAD("ic100.bin", 0x0100, 0x0080, 0x7b0c8ce5);
            /* color PROM */
            ROM_CONTINUE(0x0000, 0x0080);
            ROM_CONTINUE(0x0180, 0x0080);
            ROM_CONTINUE(0x0080, 0x0080);
            ROM_LOAD("ic89.bin", 0x0200, 0x0400, 0x1d4b02cb);
            /* 3bpp to 4bpp table */
            ROM_LOAD("ic7.bin", 0x0600, 0x0020, 0x89ba674f);/* unknown */
            ROM_END();
        }
    };

    public static InitDriverPtr init_suprloco = new InitDriverPtr() {
        public void handler() {
            /* convert graphics to 4bpp from 3bpp */

            int i, j, k, color_source, color_dest;
            UBytePtr source, dest, lookup;

            source = new UBytePtr(memory_region(REGION_GFX1));
            dest = new UBytePtr(source, 0x6000);
            lookup = new UBytePtr(memory_region(REGION_PROMS), 0x0200);

            for (i = 0; i < 0x80; i++, lookup.offset += 8) {
                for (j = 0; j < 0x40; j++, source.offset++, dest.offset++) {
                    dest.write(0, 0);
                    dest.write(0x2000, 0);
                    dest.write(0x4000, 0);
                    dest.write(0x6000, 0);

                    for (k = 0; k < 8; k++) {
                        color_source = (((source.read(0x0000) >> k) & 0x01) << 2)
                                | (((source.read(0x2000) >> k) & 0x01) << 1)
                                | (((source.read(0x4000) >> k) & 0x01) << 0);

                        color_dest = lookup.read(color_source);

                        dest.or(0x0000, (((color_dest >> 3) & 0x01) << k));
                        dest.or(0x2000, (((color_dest >> 2) & 0x01) << k));
                        dest.or(0x4000, (((color_dest >> 1) & 0x01) << k));
                        dest.or(0x6000, (((color_dest >> 0) & 0x01) << k));
                    }
                }
            }

            /* decrypt program ROMs */
            suprloco_decode();
        }
    };

    public static GameDriver driver_suprloco = new GameDriver("1982", "suprloco", "suprloco.java", rom_suprloco, null, machine_driver_suprloco, input_ports_suprloco, init_suprloco, ROT0, "Sega", "Super Locomotive");
}
