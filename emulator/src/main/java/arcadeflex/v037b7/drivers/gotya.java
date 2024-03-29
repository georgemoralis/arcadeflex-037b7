/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
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
import static arcadeflex.v037b7.mame.sndintrfH.*;
//sndhrdw imports
import static arcadeflex.v037b7.sndhrdw.gotya.*;
//sound imports
import static arcadeflex.v037b7.sound.samplesH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.gotya.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class gotya {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x5000, 0x5fff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, input_port_0_r),
                new MemoryReadAddress(0x6001, 0x6001, input_port_1_r),
                new MemoryReadAddress(0x6002, 0x6002, input_port_2_r),
                new MemoryReadAddress(0xc000, 0xd3ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x5000, 0x5fff, MWA_RAM),
                new MemoryWriteAddress(0x6004, 0x6004, gotya_video_control_w),
                new MemoryWriteAddress(0x6005, 0x6005, gotya_soundlatch_w),
                new MemoryWriteAddress(0x6006, 0x6006, MWA_RAM, gotya_scroll),
                new MemoryWriteAddress(0x6007, 0x6007, watchdog_reset_w),
                new MemoryWriteAddress(0xc000, 0xc7ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xc800, 0xcfff, colorram_w, colorram),
                new MemoryWriteAddress(0xd000, 0xd3df, MWA_RAM, gotya_foregroundram),
                new MemoryWriteAddress(0xd3e0, 0xd3ff, MWA_RAM, spriteram),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_gotya = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Paper", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Scissors", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Rock", IP_KEY_DEFAULT, IP_JOY_DEFAULT);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_DIPNAME(0x10, 0x10, "Sound Test");
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Paper", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Scissors", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Rock", IP_KEY_DEFAULT, IP_JOY_DEFAULT);

            PORT_START();
            /* DSW1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x20, "15000");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x40, "5");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the bitplanes are packed in one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{7 * 8, 6 * 8, 5 * 8, 4 * 8, 3 * 8, 2 * 8, 1 * 8, 0 * 8},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 characters */
            64, /* 64 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the bitplanes are packed in one byte */
            new int[]{0, 1, 2, 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{39 * 8, 38 * 8, 37 * 8, 36 * 8, 35 * 8, 34 * 8, 33 * 8, 32 * 8,
                7 * 8, 6 * 8, 5 * 8, 4 * 8, 3 * 8, 2 * 8, 1 * 8, 0 * 8},
            64 * 8 /* every char takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static String sample_names[]
            = { // Address triggered at
                "*gotya",
                "01.wav", /* game start tune */ // 075f
                "02.wav", /* coin in */ // 0074
                "03.wav", /* eat dot */ // 0e45
                "05.wav", /* eat dollar sign */ // 0e45

                "06.wav", /* door open */ // 19e1
                "07.wav", /* door close */ // 1965

                "08.wav", /* theme song */ // 0821
                //"09.wav"									// 1569

                /* one of these two is played after eating the last dot */
                "0a.wav", /* piccolo */ // 17af
                "0b.wav", /* tune */ // 17af

                //"0f.wav"									// 08ee
                "10.wav", /* 'We're even. Bye Bye!' */ // 162a
                "11.wav", /* 'You got me!' */ // 1657
                "12.wav", /* 'You have lost out' */ // 085e

                "13.wav", /* 'Rock' */ // 14de
                "14.wav", /* 'Scissors' */ // 14f3
                "15.wav", /* 'Paper' */ // 1508

                /* one of these is played when going by the girl between levels */
                "16.wav", /* 'Very good!' */ // 194a
                "17.wav", /* 'Wonderful!' */ // 194a
                "18.wav", /* 'Come on!' */ // 194a
                "19.wav", /* 'I love you!' */ // 194a
                "1a.wav", /* 'See you again!' */ // 194a
                null /* end of array */};

    static Samplesinterface samples_interface = new Samplesinterface(
            4, /* 4 channels */
            50, /* volume */
            sample_names
    );

    static MachineDriver machine_driver_gotya = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz ??? */
                        readmem, writemem, null, null,
                        interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            36 * 8, 32 * 8, new rectangle(0, 36 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            8, 16 * 4,
            gotya_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            gotya_vh_start,
            generic_vh_stop,
            gotya_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        samples_interface
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
    static RomLoadPtr rom_gotya = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for main CPU */
            ROM_LOAD("gb-06.bin", 0x0000, 0x1000, 0x7793985a);
            ROM_LOAD("gb-05.bin", 0x1000, 0x1000, 0x683d188b);
            ROM_LOAD("gb-04.bin", 0x2000, 0x1000, 0x15b72f09);
            ROM_LOAD("gb-03.bin", 0x3000, 0x1000, 0xf34d90ab);
            /* this is the only ROM that passes the ROM test */

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */
            ROM_LOAD("gb-12.bin", 0x0000, 0x1000, 0x4993d735);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */
            ROM_LOAD("gb-11.bin", 0x0000, 0x1000, 0x5d5eca1b);

            ROM_REGION(0x0120, REGION_PROMS);
            ROM_LOAD("prom.1a", 0x0000, 0x0020, 0x4864a5a0);
            /* color PROM */
            ROM_LOAD("prom.4c", 0x0020, 0x0100, 0x4745b5f6);
            /* lookup table */

            ROM_REGION(0x1000, REGION_USER1);
            /* no idea what these are */
            ROM_LOAD("gb-01.bin", 0x0000, 0x0800, 0xc31dba64);
            ROM_LOAD("gb-02.bin", 0x0800, 0x0800, 0x65a7e284);

            ROM_REGION(0x4000, REGION_USER2);
            /* HD38880 code? */
            ROM_LOAD("gb-10.bin", 0x0000, 0x1000, 0x8101915f);
            ROM_LOAD("gb-09.bin", 0x1000, 0x1000, 0x619bba76);
            ROM_LOAD("gb-08.bin", 0x2000, 0x1000, 0x82f59528);
            ROM_LOAD("gb-07.bin", 0x3000, 0x1000, 0x92a9f8bf);
            ROM_END();
        }
    };

    public static GameDriver driver_gotya = new GameDriver("1981", "gotya", "gotya.java", rom_gotya, null, machine_driver_gotya, input_ports_gotya, null, ROT270, "Game-A-Tron", "Got-Ya (12/24/1981, prototype?)");
}
