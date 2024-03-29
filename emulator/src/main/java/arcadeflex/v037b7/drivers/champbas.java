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
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v037b7.sound.ay8910.*;
import static arcadeflex.v037b7.sound.ay8910H.*;
import static arcadeflex.v037b7.sound.dac.*;
import static arcadeflex.v037b7.sound.dacH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.champbas.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class champbas {

    public static WriteHandlerPtr champbas_dac_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            DAC_signed_data_w.handler(0, data << 2);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x7800, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8fff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r),
                new MemoryReadAddress(0xa040, 0xa040, input_port_1_r),
                new MemoryReadAddress(0xa080, 0xa080, input_port_2_r),
                /*	new MemoryReadAddress( 0xa0a0, 0xa0a0,  ),	???? */
                new MemoryReadAddress(0xa0c0, 0xa0c0, input_port_3_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0x7000, 0x7000, AY8910_write_port_0_w),
                new MemoryWriteAddress(0x7001, 0x7001, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x7800, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8400, 0x87ff, colorram_w, colorram),
                new MemoryWriteAddress(0x8800, 0x8fef, MWA_RAM),
                new MemoryWriteAddress(0x8ff0, 0x8fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xa000, 0xa000, interrupt_enable_w),
                new MemoryWriteAddress(0xa002, 0xa002, champbas_gfxbank_w),
                new MemoryWriteAddress(0xa060, 0xa06f, MWA_RAM, spriteram_2),
                new MemoryWriteAddress(0xa080, 0xa080, soundlatch_w),
                new MemoryWriteAddress(0xa0c0, 0xa0c0, watchdog_reset_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(0xe000, 0xe3ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0x5fff, MWA_ROM),
                /*	new MemoryWriteAddress( 0x8000, 0x8000, MWA_NOP ),	unknown - maybe DAC enable */
                new MemoryWriteAddress(0xa000, 0xa000, soundlatch_w), /* probably. The sound latch has to be cleared some way */
                new MemoryWriteAddress(0xc000, 0xc000, champbas_dac_w),
                new MemoryWriteAddress(0xe000, 0xe3ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_champbas = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, "A 2/1 B 3/2");
            PORT_DIPSETTING(0x02, "A 1/1 B 2/1");
            PORT_DIPSETTING(0x01, "A 1/2 B 1/6");
            PORT_DIPSETTING(0x00, "A 1/3 B 1/6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x10, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            /* The game won't boot if set to ON */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* COIN */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3}, /* bits are packed in groups of four */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3,
                24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX1, 0x1000, spritelayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x1000, spritelayout, 0, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz ? */
            new int[]{30},
            new ReadHandlerPtr[]{input_port_0_r},
            new ReadHandlerPtr[]{input_port_1_r},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static DACinterface dac_interface = new DACinterface(
            1,
            new int[]{70}
    );

    static MachineDriver machine_driver_champbas = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 MHz (?) */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3072000, /* 3.072 MHz ? */
                        readmem2, writemem2, null, null,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 64 * 4,
            champbas_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            champbas_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_DAC,
                        dac_interface
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
    static RomLoadPtr rom_champbas = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("champbb.1", 0x0000, 0x2000, 0x218de21e);
            ROM_LOAD("champbb.2", 0x2000, 0x2000, 0x5ddd872e);
            ROM_LOAD("champbb.3", 0x4000, 0x2000, 0xf39a7046);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the speech CPU */
            ROM_LOAD("champbb.6", 0x0000, 0x2000, 0x26ab3e16);
            ROM_LOAD("champbb.7", 0x2000, 0x2000, 0x7c01715f);
            ROM_LOAD("champbb.8", 0x4000, 0x2000, 0x3c911786);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("champbb.4", 0x0000, 0x2000, 0x1930fb52);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("champbb.5", 0x0000, 0x2000, 0xa4cef5a1);

            ROM_REGION(0x0120, REGION_PROMS);
            ROM_LOAD("champbb.pr2", 0x0000, 0x020, 0x2585ffb0);/* palette */
            ROM_LOAD("champbb.pr1", 0x0020, 0x100, 0x872dd450);/* look-up table */
            ROM_END();
        }
    };

    static RomLoadPtr rom_champbbj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("11.2e", 0x0000, 0x2000, 0xe2dfc166);
            ROM_LOAD("12.2g", 0x2000, 0x2000, 0x7b4e5faa);
            ROM_LOAD("13.2h", 0x4000, 0x2000, 0xb201e31f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the speech CPU */
            ROM_LOAD("16.2k", 0x0000, 0x2000, 0x24c482ee);
            ROM_LOAD("17.2l", 0x2000, 0x2000, 0xf10b148b);
            ROM_LOAD("18.2n", 0x4000, 0x2000, 0x2dc484dd);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("14.5e", 0x0000, 0x2000, 0x1b8202b3);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("15.5g", 0x0000, 0x2000, 0xa67c0c40);

            ROM_REGION(0x0120, REGION_PROMS);
            ROM_LOAD("1e.bpr", 0x0000, 0x0020, 0xf5ce825e);/* palette */
            ROM_LOAD("5k.bpr", 0x0020, 0x0100, 0x2e481ffa);/* look-up table */
            ROM_END();
        }
    };

    static RomLoadPtr rom_champbb2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("epr5932", 0x0000, 0x2000, 0x528e3c78);
            ROM_LOAD("epr5929", 0x2000, 0x2000, 0x17b6057e);
            ROM_LOAD("epr5930", 0x4000, 0x2000, 0xb6570a90);
            ROM_LOAD("epr5931", 0x7800, 0x0800, 0x0592434d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the speech CPU */
            ROM_LOAD("epr5933", 0x0000, 0x2000, 0x26ab3e16);
            ROM_LOAD("epr5934", 0x2000, 0x2000, 0x7c01715f);
            ROM_LOAD("epr5935", 0x4000, 0x2000, 0x3c911786);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("epr5936", 0x0000, 0x2000, 0xc4a4df75);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("epr5937", 0x0000, 0x2000, 0x5c80ec42);

            ROM_REGION(0x0120, REGION_PROMS);
            ROM_LOAD("pr5957", 0x0000, 0x020, 0xf5ce825e);/* palette */
            ROM_LOAD("pr5956", 0x0020, 0x100, 0x872dd450);/* look-up table */
            ROM_END();
        }
    };

    public static GameDriver driver_champbas = new GameDriver("1983", "champbas", "champbas.java", rom_champbas, null, machine_driver_champbas, input_ports_champbas, null, ROT0, "Sega", "Champion Baseball");
    public static GameDriver driver_champbbj = new GameDriver("1983", "champbbj", "champbas.java", rom_champbbj, driver_champbas, machine_driver_champbas, input_ports_champbas, null, ROT0, "Alpha Denshi Co.", "Champion Baseball (Japan)", GAME_NOT_WORKING);
    public static GameDriver driver_champbb2 = new GameDriver("1983", "champbb2", "champbas.java", rom_champbb2, null, machine_driver_champbas, input_ports_champbas, null, ROT0, "Sega", "Champion Baseball II", GAME_NOT_WORKING);
}
