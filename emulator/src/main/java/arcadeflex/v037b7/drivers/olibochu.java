/*
 * ported to v0.37b7
 *
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.osdependH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
import static arcadeflex.v037b7.mame.mame.*;
//sound imports
import static arcadeflex.v037b7.sound.ay8910.*;
import static arcadeflex.v037b7.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class olibochu {

    public static UBytePtr olibochu_videoram = new UBytePtr();

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr olibochu_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = (color_prom.read() >> 6) & 0x01;
                bit1 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x4f * bit0 + 0xa8 * bit1));

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
 /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 0x10);
            }

            /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f));
            }
        }
    };

    public static VhUpdatePtr olibochu_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            for (offs = 0; offs < 0x400; offs++) {
                int sx, sy, attr, flipx, flipy;

                sx = offs % 32;
                sy = offs / 32;
                attr = olibochu_videoram.read(offs + 0x400);
                flipx = attr & 0x40;
                flipy = attr & 0x80;

                drawgfx(bitmap, Machine.gfx[0],
                        olibochu_videoram.read(offs) + ((attr & 0x20) << 3),
                        (attr & 0x1f) + 0x20,
                        flipx, flipy,
                        8 * sx, 8 * sy,
                        Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* 16x16 sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                int sx, sy, attr, flipx, flipy;

                sx = spriteram.read(offs + 3);
                sy = ((spriteram.read(offs + 2) + 8) & 0xff) - 8;
                attr = spriteram.read(offs + 1);
                flipx = attr & 0x40;
                flipy = attr & 0x80;

                drawgfx(bitmap, Machine.gfx[1],
                        spriteram.read(offs),
                        attr & 0x3f,
                        flipx, flipy,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* 8x8 sprites */
            for (offs = 0; offs < spriteram_2_size[0]; offs += 4) {
                int sx, sy, attr, flipx, flipy;

                sx = spriteram_2.read(offs + 3);
                sy = spriteram_2.read(offs + 2);
                attr = spriteram_2.read(offs + 1);
                flipx = attr & 0x40;
                flipy = attr & 0x80;

                drawgfx(bitmap, Machine.gfx[0],
                        spriteram_2.read(offs),
                        attr & 0x3f,
                        flipx, flipy,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };

    static int cmd;
    public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int c;

            if (offset == 0) {
                cmd = (cmd & 0x00ff) | (data << 8);
            } else {
                cmd = (cmd & 0xff00) | data;
            }

            for (c = 15; c >= 0; c--) {
                if ((cmd & (1 << c)) != 0) {
                    break;
                }
            }

            if (c >= 0) {
                soundlatch_w.handler(0, 15 - c);
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r),
                new MemoryReadAddress(0xa001, 0xa001, input_port_1_r),
                new MemoryReadAddress(0xa002, 0xa002, input_port_2_r),
                new MemoryReadAddress(0xa003, 0xa003, input_port_3_r),
                new MemoryReadAddress(0xa004, 0xa004, input_port_4_r),
                new MemoryReadAddress(0xa005, 0xa005, input_port_5_r),
                new MemoryReadAddress(0xf000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM, olibochu_videoram),
                new MemoryWriteAddress(0xa800, 0xa801, sound_command_w),
                new MemoryWriteAddress(0xa802, 0xa802, MWA_NOP), /* bit 6 = enable sound? */
                new MemoryWriteAddress(0xf000, 0xffff, MWA_RAM),
                new MemoryWriteAddress(0xf400, 0xf41f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xf440, 0xf47f, MWA_RAM, spriteram_2, spriteram_2_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x63ff, MRA_RAM),
                new MemoryReadAddress(0x7000, 0x7000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x6000, 0x63ff, MWA_RAM),
                new MemoryWriteAddress(0x7000, 0x7000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x7001, 0x7001, AY8910_write_port_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_olibochu = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);/* works in service mode but not in game */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x80, "Cross Hatch Pattern");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* In stop mode, press 2 to stop and 1 to restart */
            PORT_BITX(0x01, 0x01, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Stop Mode", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0e, 0x0e, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_5C"));
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8,
            RGN_FRAC(1, 2),
            2,
            new int[]{RGN_FRAC(1, 2), 0},
            new int[]{7, 6, 5, 4, 3, 2, 1, 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16,
            RGN_FRAC(1, 2),
            2,
            new int[]{RGN_FRAC(1, 2), 0},
            new int[]{7, 6, 5, 4, 3, 2, 1, 0,
                16 * 8 + 7, 16 * 8 + 6, 16 * 8 + 5, 16 * 8 + 4, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 256, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            2000000, /* 2 MHz ??? */
            new int[]{50}, /* volume */
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    public static InterruptPtr olibochu_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                return 0xcf;
                /* RST 08h */
            } else {
                return 0xd7;
                /* RST 10h */
            }
        }
    };

    static MachineDriver machine_driver_olibochu = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz ?? */
                        readmem, writemem, null, null,
                        olibochu_interrupt, 2
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ?? */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            32, 512,
            olibochu_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            null,
            null,
            olibochu_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static RomLoadPtr rom_olibochu = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* main CPU */
            ROM_LOAD("1b.3n", 0x0000, 0x1000, 0xbf17f4f4);
            ROM_LOAD("2b.3lm", 0x1000, 0x1000, 0x63833b0d);
            ROM_LOAD("3b.3k", 0x2000, 0x1000, 0xa4038e8b);
            ROM_LOAD("4b.3j", 0x3000, 0x1000, 0xaad4bec4);
            ROM_LOAD("5b.3h", 0x4000, 0x1000, 0x66efa79f);
            ROM_LOAD("6b.3f", 0x5000, 0x1000, 0x1123d1ef);
            ROM_LOAD("7c.3e", 0x6000, 0x1000, 0x89c26fb4);
            ROM_LOAD("8b.3d", 0x7000, 0x1000, 0xaf19e5a5);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("17.4j", 0x0000, 0x1000, 0x57f07402);
            ROM_LOAD("18.4l", 0x1000, 0x1000, 0x0a903e9c);

            ROM_REGION(0x2000, REGION_SOUND1);/* samples? */
            ROM_LOAD("15.1k", 0x0000, 0x1000, 0xfb5dd281);
            ROM_LOAD("16.1m", 0x1000, 0x1000, 0xc07614a5);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("13.6n", 0x0000, 0x1000, 0xb4fcf9af);
            ROM_LOAD("14.4n", 0x1000, 0x1000, 0xaf54407e);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9.6a", 0x0000, 0x1000, 0xfa69e16e);
            ROM_LOAD("10.2a", 0x1000, 0x1000, 0x10359f84);
            ROM_LOAD("11.4a", 0x2000, 0x1000, 0x1d968f5f);
            ROM_LOAD("12.2a", 0x3000, 0x1000, 0xd8f0c157);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("c-1", 0x0000, 0x0020, 0xe488e831);/* palette */
            ROM_LOAD("c-2", 0x0020, 0x0100, 0x698a3ba0);/* sprite lookup table */
            ROM_LOAD("c-3", 0x0120, 0x0100, 0xefc4e408);/* char lookup table */
            ROM_END();
        }
    };

    public static GameDriver driver_olibochu = new GameDriver("1981", "olibochu", "olibochu.java", rom_olibochu, null, machine_driver_olibochu, input_ports_olibochu, null, ROT270, "Irem + GDI", "Oli-Boo-Chu", GAME_WRONG_COLORS | GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL);
}
