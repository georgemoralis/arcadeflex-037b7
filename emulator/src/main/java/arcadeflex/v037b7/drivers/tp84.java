/**
 * ported to v0.37b7
 * ported to v0.36
 *
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v037b7.sound.streams.*;
import static arcadeflex.v058.sound.sn76496.*;
import static arcadeflex.v058.sound.sn76496H.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;
import static arcadeflex.v037b7.vidhrdw.tp84.*;

public class tp84 {

    static UBytePtr sharedram = new UBytePtr();

    public static ReadHandlerPtr sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return sharedram.read(offset);
        }
    };

    public static WriteHandlerPtr sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sharedram.write(offset, data);
        }
    };
    /* JB 970829 - just give it what it wants
		F104: LDX   $6400
		F107: LDU   $6402
		F10A: LDA   $640B
		F10D: BEQ   $F13B
		F13B: LDX   $6404
		F13E: LDU   $6406
		F141: LDA   $640C
		F144: BEQ   $F171
		F171: LDA   $2000	; read beam
		F174: ADDA  #$20
		F176: BCC   $F104
     */
    public static ReadHandlerPtr tp84_beam_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //	return cpu_getscanline();
            return 255;
            /* always return beam position 255 */ /* JB 970829 */
        }
    };

    /* JB 970829 - catch a busy loop for CPU 1
		E0ED: LDA   #$01
		E0EF: STA   $4000
		E0F2: BRA   $E0ED
     */
    public static WriteHandlerPtr tp84_catchloop_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (cpu_get_pc() == 0xe0f2) {
                cpu_spinuntil_int();
            }
        }
    };

    public static ReadHandlerPtr tp84_sh_timer_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* main xtal 14.318MHz, divided by 4 to get the CPU clock, further */
 /* divided by 2048 to get this timer */
 /* (divide by (2048/2), and not 1024, because the CPU cycle counter is */
 /* incremented every other state change of the clock) */
            return (cpu_gettotalcycles() / (2048 / 2)) & 0x0f;
        }
    };

    public static WriteHandlerPtr tp84_filter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int C;

            /* 76489 #0 */
            C = 0;
            if ((offset & 0x008) != 0) {
                C += 47000;
                /*  47000pF = 0.047uF */
            }
            if ((offset & 0x010) != 0) {
                C += 470000;
                /* 470000pF = 0.47uF */
            }
            set_RC_filter(0, 1000, 2200, 1000, C);

            /* 76489 #1 (optional) */
            C = 0;
            if ((offset & 0x020) != 0) {
                C += 47000;
                /*  47000pF = 0.047uF */
            }
            if ((offset & 0x040) != 0) {
                C += 470000;
                /* 470000pF = 0.47uF */
            }
            //	set_RC_filter(1,1000,2200,1000,C);

            /* 76489 #2 */
            C = 0;
            if ((offset & 0x080) != 0) {
                C += 470000;
                /* 470000pF = 0.47uF */
            }
            set_RC_filter(1, 1000, 2200, 1000, C);

            /* 76489 #3 */
            C = 0;
            if ((offset & 0x100) != 0) {
                C += 470000;
                /* 470000pF = 0.47uF */
            }
            set_RC_filter(2, 1000, 2200, 1000, C);
        }
    };

    public static WriteHandlerPtr tp84_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_cause_interrupt(2, 0xff);
        }
    };

    /* CPU 1 read addresses */
    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x2800, 0x2800, input_port_0_r),
                new MemoryReadAddress(0x2820, 0x2820, input_port_1_r),
                new MemoryReadAddress(0x2840, 0x2840, input_port_2_r),
                new MemoryReadAddress(0x2860, 0x2860, input_port_3_r),
                new MemoryReadAddress(0x3000, 0x3000, input_port_4_r),
                new MemoryReadAddress(0x4000, 0x4fff, MRA_RAM),
                new MemoryReadAddress(0x5000, 0x57ff, sharedram_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    /* CPU 1 write addresses */
    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x2000, 0x2000, MWA_RAM), /*Watch dog?*/
                new MemoryWriteAddress(0x2800, 0x2800, tp84_col0_w),
                new MemoryWriteAddress(0x3000, 0x3000, MWA_RAM),
                new MemoryWriteAddress(0x3800, 0x3800, tp84_sh_irqtrigger_w),
                new MemoryWriteAddress(0x3a00, 0x3a00, soundlatch_w),
                new MemoryWriteAddress(0x3c00, 0x3c00, MWA_RAM, tp84_scrollx), /* Y scroll */
                new MemoryWriteAddress(0x3e00, 0x3e00, MWA_RAM, tp84_scrolly), /* X scroll */
                new MemoryWriteAddress(0x4000, 0x43ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x4400, 0x47ff, tp84_videoram2_w, tp84_videoram2),
                new MemoryWriteAddress(0x4800, 0x4bff, colorram_w, colorram),
                new MemoryWriteAddress(0x4c00, 0x4fff, tp84_colorram2_w, tp84_colorram2),
                new MemoryWriteAddress(0x5000, 0x57ff, sharedram_w, sharedram),
                new MemoryWriteAddress(0x5000, 0x5177, MWA_RAM, spriteram, spriteram_size), /* FAKE (see below) */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    /* CPU 2 read addresses */
    static MemoryReadAddress readmem_cpu2[]
            = {
                new MemoryReadAddress(0x0000, 0x0000, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2000, tp84_beam_r), /* beam position */
                new MemoryReadAddress(0x6000, 0x67ff, MRA_RAM),
                new MemoryReadAddress(0x8000, 0x87ff, sharedram_r),
                new MemoryReadAddress(0xe000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    /* CPU 2 write addresses */
    static MemoryWriteAddress writemem_cpu2[]
            = {
                new MemoryWriteAddress(0x0000, 0x0000, MWA_RAM), /* Watch dog ?*/
                new MemoryWriteAddress(0x4000, 0x4000, tp84_catchloop_w), /* IRQ enable */ /* JB 970829 */
                new MemoryWriteAddress(0x6000, 0x67ff, MWA_RAM),
                //	new MemoryWriteAddress( 0x67a0, 0x67ff, MWA_RAM, spriteram, spriteram_size ),	/* REAL (multiplexed) */
                new MemoryWriteAddress(0x8000, 0x87ff, sharedram_w),
                new MemoryWriteAddress(0xe000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(0x8000, 0x8000, tp84_sh_timer_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa1ff, tp84_filter_w),
                new MemoryWriteAddress(0xc000, 0xc000, MWA_NOP),
                new MemoryWriteAddress(0xc001, 0xc001, SN76496_0_w),
                new MemoryWriteAddress(0xc003, 0xc003, SN76496_1_w),
                new MemoryWriteAddress(0xc004, 0xc004, SN76496_2_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_tp84 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "Invalid");

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x18, "Bonus");
            PORT_DIPSETTING(0x18, "10000 50000");
            PORT_DIPSETTING(0x10, "20000 60000");
            PORT_DIPSETTING(0x08, "30000 70000");
            PORT_DIPSETTING(0x00, "40000 80000");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Medium");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            2, /* 2 bits per pixel */
            new int[]{4, 0}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3}, /* bits are packed in groups of four */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel */
            new int[]{256 * 64 * 8 + 4, 256 * 64 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64 * 8),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 64 * 4 * 8, 16 * 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static SN76496interface sn76496_interface = new SN76496interface(
            3, /* 3 chips */
            new int[]{14318180 / 8, 14318180 / 8, 14318180 / 8},
            new int[]{75, 75, 75}
    );

    static MachineDriver machine_driver_tp84 = new MachineDriver(
            /* basic machine hardware  */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1500000, /* ??? */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        1500000, /* ??? */
                        readmem_cpu2, writemem_cpu2, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318180 / 4,
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            100, /* 100 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo, /* GfxDecodeInfo * */
            256, 4096, /* see tp84_vh_convert_color_prom for explanation */
            tp84_vh_convert_color_prom, /* convert color prom routine */
            VIDEO_TYPE_RASTER,
            null, /* vh_init routine */
            tp84_vh_start, /* vh_start routine */
            tp84_vh_stop, /* vh_stop routine */
            tp84_vh_screenrefresh, /* vh_update routine */
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
    static RomLoadPtr rom_tp84 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("tp84_7j.bin", 0x8000, 0x2000, 0x605f61c7);
            ROM_LOAD("tp84_8j.bin", 0xa000, 0x2000, 0x4b4629a4);
            ROM_LOAD("tp84_9j.bin", 0xc000, 0x2000, 0xdbd5333b);
            ROM_LOAD("tp84_10j.bin", 0xe000, 0x2000, 0xa45237c4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("tp84_10d.bin", 0xe000, 0x2000, 0x36462ff1);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for code of sound cpu Z80 */

            ROM_LOAD("tp84s_6a.bin", 0x0000, 0x2000, 0xc44414da);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tp84_2j.bin", 0x0000, 0x2000, 0x05c7508f);/* chars */

            ROM_LOAD("tp84_1j.bin", 0x2000, 0x2000, 0x498d90b7);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tp84_12a.bin", 0x0000, 0x2000, 0xcd682f30);/* sprites */

            ROM_LOAD("tp84_13a.bin", 0x2000, 0x2000, 0x888d4bd6);
            ROM_LOAD("tp84_14a.bin", 0x4000, 0x2000, 0x9a220b39);
            ROM_LOAD("tp84_15a.bin", 0x6000, 0x2000, 0xfac98397);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("tp84_2c.bin", 0x0000, 0x0100, 0xd737eaba);/* palette red component */

            ROM_LOAD("tp84_2d.bin", 0x0100, 0x0100, 0x2f6a9a2a);/* palette green component */

            ROM_LOAD("tp84_1e.bin", 0x0200, 0x0100, 0x2e21329b);/* palette blue component */

            ROM_LOAD("tp84_1f.bin", 0x0300, 0x0100, 0x61d2d398);/* char lookup table */

            ROM_LOAD("tp84_16c.bin", 0x0400, 0x0100, 0x13c4e198);/* sprite lookup table */

            ROM_END();
        }
    };

    static RomLoadPtr rom_tp84a = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("tp84_7j.bin", 0x8000, 0x2000, 0x605f61c7);
            ROM_LOAD("f05", 0xa000, 0x2000, 0xe97d5093);
            ROM_LOAD("tp84_9j.bin", 0xc000, 0x2000, 0xdbd5333b);
            ROM_LOAD("f07", 0xe000, 0x2000, 0x8fbdb4ef);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("tp84_10d.bin", 0xe000, 0x2000, 0x36462ff1);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for code of sound cpu Z80 */

            ROM_LOAD("tp84s_6a.bin", 0x0000, 0x2000, 0xc44414da);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tp84_2j.bin", 0x0000, 0x2000, 0x05c7508f);/* chars */

            ROM_LOAD("tp84_1j.bin", 0x2000, 0x2000, 0x498d90b7);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tp84_12a.bin", 0x0000, 0x2000, 0xcd682f30);/* sprites */

            ROM_LOAD("tp84_13a.bin", 0x2000, 0x2000, 0x888d4bd6);
            ROM_LOAD("tp84_14a.bin", 0x4000, 0x2000, 0x9a220b39);
            ROM_LOAD("tp84_15a.bin", 0x6000, 0x2000, 0xfac98397);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("tp84_2c.bin", 0x0000, 0x0100, 0xd737eaba);/* palette red component */

            ROM_LOAD("tp84_2d.bin", 0x0100, 0x0100, 0x2f6a9a2a);/* palette green component */

            ROM_LOAD("tp84_1e.bin", 0x0200, 0x0100, 0x2e21329b);/* palette blue component */

            ROM_LOAD("tp84_1f.bin", 0x0300, 0x0100, 0x61d2d398);/* char lookup table */

            ROM_LOAD("tp84_16c.bin", 0x0400, 0x0100, 0x13c4e198);/* sprite lookup table */

            ROM_END();
        }
    };

    public static GameDriver driver_tp84 = new GameDriver("1984", "tp84", "tp84.java", rom_tp84, null, machine_driver_tp84, input_ports_tp84, null, ROT90, "Konami", "Time Pilot '84 (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_tp84a = new GameDriver("1984", "tp84a", "tp84.java", rom_tp84a, driver_tp84, machine_driver_tp84, input_ports_tp84, null, ROT90, "Konami", "Time Pilot '84 (set 2)", GAME_NO_COCKTAIL);
}
