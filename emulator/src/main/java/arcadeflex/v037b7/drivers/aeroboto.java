/*
 * ported to v0.37b7
 *
 */
package arcadeflex.v037b7.drivers;

//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;
//sound imports
import static arcadeflex.v037b7.sound.ay8910.*;
import static arcadeflex.v037b7.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.aeroboto.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class aeroboto {

    static int player;

    public static ReadHandlerPtr aeroboto_in0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(player);
        }
    };
    static int count;
    public static ReadHandlerPtr aeroboto_201_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* if you keep a button pressed during boot, the game will expect this */
 /* serie of values to be returned from 3004, and display "PASS 201" if it is */
            int res[] = {0xff, 0x9f, 0x1b, 0x03};
            logerror("PC %04x: read 3004\n", cpu_get_pc());
            return res[(count++) & 3];
        }
    };

    public static WriteHandlerPtr aeroboto_3000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 selects player1/player2 controls */
            player = data & 1;

            /* not sure about this, could be bit 2 */
            aeroboto_charbank = (data & 0x02) >> 1;

            /* there's probably a flip screen here as well */
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x08ff, MRA_RAM), /* ? copied to 2000 */
                new MemoryReadAddress(0x1000, 0x17ff, MRA_RAM),
                new MemoryReadAddress(0x1800, 0x183f, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x28ff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3000, aeroboto_in0_r),
                new MemoryReadAddress(0x3001, 0x3001, input_port_2_r),
                new MemoryReadAddress(0x3002, 0x3002, input_port_3_r),
                new MemoryReadAddress(0x3004, 0x3004, aeroboto_201_r),
                new MemoryReadAddress(0x3800, 0x3800, watchdog_reset_r), /* or IRQ acknowledge */
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x08ff, MWA_RAM), /* ? initialized on startup */
                new MemoryWriteAddress(0x0900, 0x09ff, MWA_RAM), /* ? initialized on startup (same as 0800) */
                new MemoryWriteAddress(0x1000, 0x13ff, MWA_RAM, aeroboto_videoram),
                new MemoryWriteAddress(0x1400, 0x17ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1800, 0x181f, MWA_RAM, aeroboto_fgscroll),
                new MemoryWriteAddress(0x1820, 0x183f, MWA_RAM, aeroboto_bgscroll),
                new MemoryWriteAddress(0x2000, 0x20ff, MWA_RAM), /* scroll? maybe stars? copied from 0800 */
                new MemoryWriteAddress(0x2800, 0x28ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x3000, aeroboto_3000_w),
                new MemoryWriteAddress(0x3001, 0x3001, soundlatch_w), /* ? */
                new MemoryWriteAddress(0x3002, 0x3002, soundlatch2_w), /* ? */
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_RAM),
                new MemoryReadAddress(0x9002, 0x9002, AY8910_read_port_0_r),
                new MemoryReadAddress(0xa002, 0xa002, AY8910_read_port_1_r),
                new MemoryReadAddress(0xf000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x9000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x9001, 0x9001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0xa000, 0xa000, AY8910_control_port_1_w),
                new MemoryWriteAddress(0xa001, 0xa001, AY8910_write_port_1_w),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_formatz = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x0c, "30000");
            PORT_DIPSETTING(0x08, "40000");
            PORT_DIPSETTING(0x04, "70000");
            PORT_DIPSETTING(0x00, "100000");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            /* the coin input must stay low for exactly 2 frames to be consistently recognized. */
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 2);

            PORT_START();
            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x07, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x18, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x18, "Hardest");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            /* the manual lists the flip screen in the previous bank (replacing Coin A) */
            PORT_DIPNAME(0x80, 0x00, "Flip Screen?");
            PORT_DIPSETTING(0x00, "Off?");
            PORT_DIPSETTING(0x80, "On?");
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 chars */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{4, 0},
            new int[]{0, 1, 2, 3, 512 * 8 * 8 + 0, 512 * 8 * 8 + 1, 512 * 8 * 8 + 2, 512 * 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            8, 16, /* 8*16 sprites */
            256, /* 128 sprites */
            3, /* 3 bits per pixel */
            new int[]{2 * 256 * 16 * 8, 256 * 16 * 8, 0}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            16 * 8 /* every sprite takes 16 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64), /* chars */
                new GfxDecodeInfo(REGION_GFX2, 0, charlayout, 0, 64), /* sky */
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 0, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ? (hand tuned) */
            new int[]{25, 25},
            new ReadHandlerPtr[]{soundlatch_r, null}, /* ? */
            new ReadHandlerPtr[]{soundlatch2_r, null}, /* ? */
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_formatz = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 Mhz ? */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        1250000, /* 1.25 Mhz ? */
                        readmem_sound, writemem_sound, null, null,
                        interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            null,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            aeroboto_vh_screenrefresh,
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
    static RomLoadPtr rom_formatz = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for main CPU */
            ROM_LOAD("format_z.8", 0x4000, 0x4000, 0x81a2416c);
            ROM_LOAD("format_z.7", 0x8000, 0x4000, 0x986e6052);
            ROM_LOAD("format_z.6", 0xc000, 0x4000, 0xbaa0d745);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for sound CPU */
            ROM_LOAD("format_z.9", 0xf000, 0x1000, 0x6b9215ad);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("format_z.5", 0x0000, 0x2000, 0xba50be57);/* characters */

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("format_z.4", 0x0000, 0x2000, 0x910375a0);/* characters */

            ROM_REGION(0x3000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("format_z.1", 0x0000, 0x1000, 0x5739afd2);/* sprites */
            ROM_LOAD("format_z.2", 0x1000, 0x1000, 0x3a821391);/* sprites */
            ROM_LOAD("format_z.3", 0x2000, 0x1000, 0x7d1aec79);/* sprites */

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("10a", 0x0000, 0x0100, 0x00000000);
            ROM_LOAD("10b", 0x0100, 0x0100, 0x00000000);
            ROM_LOAD("10c", 0x0200, 0x0100, 0x00000000);
            ROM_END();
        }
    };

    static RomLoadPtr rom_aeroboto = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for main CPU */
            ROM_LOAD("aeroboto.8", 0x4000, 0x4000, 0x4d3fc049);
            ROM_LOAD("aeroboto.7", 0x8000, 0x4000, 0x522f51c1);
            ROM_LOAD("aeroboto.6", 0xc000, 0x4000, 0x1a295ffb);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for sound CPU */
            ROM_LOAD("format_z.9", 0xf000, 0x1000, 0x6b9215ad);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("aeroboto.5", 0x0000, 0x2000, 0x32fc00f9);/* characters */

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("format_z.4", 0x0000, 0x2000, 0x910375a0);/* characters */

            ROM_REGION(0x3000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("aeroboto.1", 0x0000, 0x1000, 0x7820eeaf);/* sprites */
            ROM_LOAD("aeroboto.2", 0x1000, 0x1000, 0xc7f81a3c);/* sprites */
            ROM_LOAD("aeroboto.3", 0x2000, 0x1000, 0x5203ad04);/* sprites */

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("10a", 0x0000, 0x0100, 0x00000000);
            ROM_LOAD("10b", 0x0100, 0x0100, 0x00000000);
            ROM_LOAD("10c", 0x0200, 0x0100, 0x00000000);
            ROM_END();
        }
    };

    public static GameDriver driver_formatz = new GameDriver("1984", "formatz", "aeroboto.java", rom_formatz, null, machine_driver_formatz, input_ports_formatz, null, ROT0, "Jaleco", "Formation Z", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_aeroboto = new GameDriver("1984", "aeroboto", "aeroboto.java", rom_aeroboto, driver_formatz, machine_driver_formatz, input_ports_formatz, null, ROT0, "[Jaleco] (Williams license)", "Aeroboto", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
}
