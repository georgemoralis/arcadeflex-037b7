/*
 * ported to v0.37b7
 *
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
//cpu imports
import static arcadeflex.v037b7.cpu.m6809.m6809H.*;
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
import static arcadeflex.v037b7.sound._2203intf.*;
import static arcadeflex.v037b7.sound._2203intfH.*;
import static arcadeflex.v037b7.sound._3526intf.*;
import static arcadeflex.v037b7.sound._3812intfH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.brkthru.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class brkthru {

    public static UBytePtr brkthru_nmi_enable = new UBytePtr();
    /* needs to be tracked down */

    static int nmi_enable;

    public static WriteHandlerPtr brkthru_1803_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 = NMI enable */
            nmi_enable = ~data & 1;

            /* bit 1 = ? maybe IRQ acknowledge */
        }
    };
    public static WriteHandlerPtr darwin_0803_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 = NMI enable */
 /*nmi_enable = ~data & 1;*/
            logerror("0803 %02X\n", data);
            nmi_enable = data;
            /* bit 1 = ? maybe IRQ acknowledge */
        }
    };

    public static WriteHandlerPtr brkthru_soundlatch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, M6809_INT_NMI);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM), /* Plane 0: Text */
                new MemoryReadAddress(0x0400, 0x0bff, MRA_RAM),
                new MemoryReadAddress(0x0c00, 0x0fff, MRA_RAM), /* Plane 2  Background */
                new MemoryReadAddress(0x1000, 0x10ff, MRA_RAM), /* Plane 1: Sprites */
                new MemoryReadAddress(0x1100, 0x17ff, MRA_RAM),
                new MemoryReadAddress(0x1800, 0x1800, input_port_0_r), /* player controls, player start */
                new MemoryReadAddress(0x1801, 0x1801, input_port_1_r), /* cocktail player controls */
                new MemoryReadAddress(0x1802, 0x1802, input_port_3_r), /* DSW 0 */
                new MemoryReadAddress(0x1803, 0x1803, input_port_2_r), /* coin input  DSW */
                new MemoryReadAddress(0x2000, 0x3fff, MRA_BANK1),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM, brkthru_videoram, brkthru_videoram_size),
                new MemoryWriteAddress(0x0400, 0x0bff, MWA_RAM),
                new MemoryWriteAddress(0x0c00, 0x0fff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1000, 0x10ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x1100, 0x17ff, MWA_RAM),
                new MemoryWriteAddress(0x1800, 0x1801, brkthru_1800_w), /* bg scroll and color, ROM bank selection, flip screen */
                new MemoryWriteAddress(0x1802, 0x1802, brkthru_soundlatch_w),
                new MemoryWriteAddress(0x1803, 0x1803, brkthru_1803_w), /* NMI enable, + ? */
                new MemoryWriteAddress(0x2000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress darwin_readmem[]
            = {
                new MemoryReadAddress(0x1000, 0x13ff, MRA_RAM), /* Plane 0: Text */
                new MemoryReadAddress(0x0400, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x1c00, 0x1fff, MRA_RAM), /* Plane 2  Background */
                new MemoryReadAddress(0x0000, 0x00ff, MRA_RAM), /* Plane 1: Sprites */
                new MemoryReadAddress(0x1400, 0x1bff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x0800, input_port_0_r), /* player controls, player start */
                new MemoryReadAddress(0x0801, 0x0801, input_port_1_r), /* cocktail player controls */
                new MemoryReadAddress(0x0802, 0x0802, input_port_3_r), /* DSW 0 */
                new MemoryReadAddress(0x0803, 0x0803, input_port_2_r), /* coin input  DSW */
                new MemoryReadAddress(0x2000, 0x3fff, MRA_BANK1),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress darwin_writemem[]
            = {
                new MemoryWriteAddress(0x1000, 0x13ff, MWA_RAM, brkthru_videoram, brkthru_videoram_size),
                new MemoryWriteAddress(0x1c00, 0x1fff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x0000, 0x00ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x1400, 0x1bff, MWA_RAM),
                new MemoryWriteAddress(0x0100, 0x01ff, MWA_NOP), /*tidyup, nothing realy here?*/
                new MemoryWriteAddress(0x0800, 0x0801, brkthru_1800_w), /* bg scroll and color, ROM bank selection, flip screen */
                new MemoryWriteAddress(0x0802, 0x0802, brkthru_soundlatch_w),
                new MemoryWriteAddress(0x0803, 0x0803, darwin_0803_w), /* NMI enable, + ? */
                new MemoryWriteAddress(0x2000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0x4000, soundlatch_r),
                new MemoryReadAddress(0x6000, 0x6000, YM2203_status_port_0_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x2000, YM3526_control_port_0_w),
                new MemoryWriteAddress(0x2001, 0x2001, YM3526_write_port_0_w),
                new MemoryWriteAddress(0x6000, 0x6000, YM2203_control_port_0_w),
                new MemoryWriteAddress(0x6001, 0x6001, YM2203_write_port_0_w),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    public static InterruptPtr brkthru_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                if (nmi_enable != 0) {
                    return nmi_interrupt.handler();
                }
            } else {
                /* generate IRQ on coin insertion */
                if ((readinputport(2) & 0xe0) != 0xe0) {
                    return interrupt.handler();
                }
            }

            return ignore_interrupt.handler();
        }
    };

    static InputPortPtr input_ports_brkthru = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);/* used only by the self test */

            PORT_START();
            /* IN2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "99", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x10, DEF_STR("Yes"));
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN1, 2);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 2);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_SERVICE1, 2);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_darwin = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START();
            /* IN2 modified by Shingo Suzuki 1999/11/02 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x02, "20k 50k and every 50k");
            PORT_DIPSETTING(0x00, "30k 80k and every 80k");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x0c, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN1, 2);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 2);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_SERVICE1, 2);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 chars */
            256, /* 256 characters */
            3, /* 3 bits per pixel */
            new int[]{512 * 8 * 8 + 4, 0, 4}, /* plane offset */
            new int[]{256 * 8 * 8 + 0, 256 * 8 * 8 + 1, 256 * 8 * 8 + 2, 256 * 8 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout tilelayout1 = new GfxLayout(
            16, 16, /* 16*16 tiles */
            128, /* 128 tiles */
            3, /* 3 bits per pixel */
            new int[]{0x4000 * 8 + 4, 0, 4}, /* plane offset */
            new int[]{0, 1, 2, 3, 1024 * 8 * 8 + 0, 1024 * 8 * 8 + 1, 1024 * 8 * 8 + 2, 1024 * 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 1024 * 8 * 8 + 0, 16 * 8 + 1024 * 8 * 8 + 1, 16 * 8 + 1024 * 8 * 8 + 2, 16 * 8 + 1024 * 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );

    static GfxLayout tilelayout2 = new GfxLayout(
            16, 16, /* 16*16 tiles */
            128, /* 128 tiles */
            3, /* 3 bits per pixel */
            new int[]{0x3000 * 8 + 0, 0, 4}, /* plane offset */
            new int[]{0, 1, 2, 3, 1024 * 8 * 8 + 0, 1024 * 8 * 8 + 1, 1024 * 8 * 8 + 2, 1024 * 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 1024 * 8 * 8 + 0, 16 * 8 + 1024 * 8 * 8 + 1, 16 * 8 + 1024 * 8 * 8 + 2, 16 * 8 + 1024 * 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            1024, /* 1024 sprites */
            3, /* 3 bits per pixel */
            new int[]{2 * 1024 * 32 * 8, 1024 * 32 * 8, 0}, /* plane offset */
            new int[]{16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7,
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 0, 1),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tilelayout1, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x01000, tilelayout2, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x08000, tilelayout1, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x09000, tilelayout2, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x10000, tilelayout1, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x11000, tilelayout2, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, tilelayout1, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x19000, tilelayout2, 8 + 8 * 8, 16),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, spritelayout, 8, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    /* handler called by the 3812 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int state) {
            cpu_set_irq_line(1, 0, state);
            //cpu_cause_interrupt(1,M6809_INT_IRQ);
        }
    };

    static YM2203interface ym2203_interface = new YM2203interface(
            1,
            1500000, /* Unknown */
            new int[]{YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static YM3526interface ym3526_interface = new YM3526interface(
            1, /* 1 chip (no more supported) */
            3000000, /* 3.000000 MHz ? (partially supported) */
            new int[]{255}, /* (not supported) */
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static MachineDriver machine_driver_brkthru = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 Mhz ? */
                        readmem, writemem, null, null,
                        brkthru_interrupt, 2
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        1250000, /* 1.25 Mhz ? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are caused by the YM3526 */
                )
            },
            58, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration (not sure) */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 1 * 8, 31 * 8 - 1), /* not sure */
            gfxdecodeinfo,
            256, 8 + 8 * 8 + 16 * 8,
            brkthru_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            brkthru_vh_start,
            brkthru_vh_stop,
            brkthru_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_YM3526,
                        ym3526_interface
                )
            }
    );

    static MachineDriver machine_driver_darwin = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1500000, /* 1.25 Mhz ? */
                        darwin_readmem, darwin_writemem, null, null,
                        brkthru_interrupt, 2
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        1500000, /* 1.25 Mhz ? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are caused by the YM3526 */
                )
            },
            58, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration / 60.58 tuned by Shingo Suzuki 1999/10/16 */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            256, 8 + 8 * 8 + 16 * 8,
            brkthru_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            brkthru_vh_start,
            brkthru_vh_stop,
            brkthru_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_YM3526,
                        ym3526_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static RomLoadPtr rom_brkthru = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for main CPU + 64k for banked ROMs */
            ROM_LOAD("brkthru.1", 0x04000, 0x4000, 0xcfb4265f);
            ROM_LOAD("brkthru.2", 0x08000, 0x8000, 0xfa8246d9);
            ROM_LOAD("brkthru.4", 0x10000, 0x8000, 0x8cabf252);
            ROM_LOAD("brkthru.3", 0x18000, 0x8000, 0x2f2c40c2);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("brkthru.12", 0x00000, 0x2000, 0x58c0b29b);/* characters */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            /* background */
 /* we do a lot of scatter loading here, to place the data in a format */
 /* which can be decoded by MAME's standard functions */
            ROM_LOAD("brkthru.7", 0x00000, 0x4000, 0x920cc56a);/* bitplanes 1,2 for bank 1,2 */
            ROM_CONTINUE(0x08000, 0x4000);
            /* bitplanes 1,2 for bank 3,4 */
            ROM_LOAD("brkthru.6", 0x10000, 0x4000, 0xfd3cee40);/* bitplanes 1,2 for bank 5,6 */
            ROM_CONTINUE(0x18000, 0x4000);
            /* bitplanes 1,2 for bank 7,8 */
            ROM_LOAD("brkthru.8", 0x04000, 0x1000, 0xf67ee64e);/* bitplane 3 for bank 1,2 */
            ROM_CONTINUE(0x06000, 0x1000);
            ROM_CONTINUE(0x0c000, 0x1000);
            /* bitplane 3 for bank 3,4 */
            ROM_CONTINUE(0x0e000, 0x1000);
            ROM_CONTINUE(0x14000, 0x1000);
            /* bitplane 3 for bank 5,6 */
            ROM_CONTINUE(0x16000, 0x1000);
            ROM_CONTINUE(0x1c000, 0x1000);
            /* bitplane 3 for bank 7,8 */
            ROM_CONTINUE(0x1e000, 0x1000);

            ROM_REGION(0x18000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("brkthru.9", 0x00000, 0x8000, 0xf54e50a7);/* sprites */
            ROM_LOAD("brkthru.10", 0x08000, 0x8000, 0xfd156945);
            ROM_LOAD("brkthru.11", 0x10000, 0x8000, 0xc152a99b);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("brkthru.13", 0x0000, 0x0100, 0xaae44269);/* red and green component */
            ROM_LOAD("brkthru.14", 0x0100, 0x0100, 0xf2d4822a);/* blue component */

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */
            ROM_LOAD("brkthru.5", 0x8000, 0x8000, 0xc309435f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_brkthruj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for main CPU + 64k for banked ROMs */
            ROM_LOAD("1", 0x04000, 0x4000, 0x09bd60ee);
            ROM_LOAD("2", 0x08000, 0x8000, 0xf2b2cd1c);
            ROM_LOAD("4", 0x10000, 0x8000, 0xb42b3359);
            ROM_LOAD("brkthru.3", 0x18000, 0x8000, 0x2f2c40c2);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("12", 0x00000, 0x2000, 0x3d9a7003);/* characters */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            /* background */
 /* we do a lot of scatter loading here, to place the data in a format */
 /* which can be decoded by MAME's standard functions */
            ROM_LOAD("brkthru.7", 0x00000, 0x4000, 0x920cc56a);/* bitplanes 1,2 for bank 1,2 */
            ROM_CONTINUE(0x08000, 0x4000);
            /* bitplanes 1,2 for bank 3,4 */
            ROM_LOAD("6", 0x10000, 0x4000, 0xcb47b395);/* bitplanes 1,2 for bank 5,6 */
            ROM_CONTINUE(0x18000, 0x4000);
            /* bitplanes 1,2 for bank 7,8 */
            ROM_LOAD("8", 0x04000, 0x1000, 0x5e5a2cd7);/* bitplane 3 for bank 1,2 */
            ROM_CONTINUE(0x06000, 0x1000);
            ROM_CONTINUE(0x0c000, 0x1000);
            /* bitplane 3 for bank 3,4 */
            ROM_CONTINUE(0x0e000, 0x1000);
            ROM_CONTINUE(0x14000, 0x1000);
            /* bitplane 3 for bank 5,6 */
            ROM_CONTINUE(0x16000, 0x1000);
            ROM_CONTINUE(0x1c000, 0x1000);
            /* bitplane 3 for bank 7,8 */
            ROM_CONTINUE(0x1e000, 0x1000);

            ROM_REGION(0x18000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("brkthru.9", 0x00000, 0x8000, 0xf54e50a7);/* sprites */
            ROM_LOAD("brkthru.10", 0x08000, 0x8000, 0xfd156945);
            ROM_LOAD("brkthru.11", 0x10000, 0x8000, 0xc152a99b);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("brkthru.13", 0x0000, 0x0100, 0xaae44269);/* red and green component */
            ROM_LOAD("brkthru.14", 0x0100, 0x0100, 0xf2d4822a);/* blue component */

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */
            ROM_LOAD("brkthru.5", 0x8000, 0x8000, 0xc309435f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_darwin = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for main CPU + 64k for banked ROMs */
            ROM_LOAD("darw_04.rom", 0x04000, 0x4000, 0x0eabf21c);
            ROM_LOAD("darw_05.rom", 0x08000, 0x8000, 0xe771f864);
            ROM_LOAD("darw_07.rom", 0x10000, 0x8000, 0x97ac052c);
            ROM_LOAD("darw_06.rom", 0x18000, 0x8000, 0x2a9fb208);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("darw_09.rom", 0x00000, 0x2000, 0x067b4cf5);
            /* characters */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            /* background */
 /* we do a lot of scatter loading here, to place the data in a format */
 /* which can be decoded by MAME's standard functions */
            ROM_LOAD("darw_03.rom", 0x00000, 0x4000, 0x57d0350d);/* bitplanes 1,2 for bank 1,2 */
            ROM_CONTINUE(0x08000, 0x4000);
            /* bitplanes 1,2 for bank 3,4 */
            ROM_LOAD("darw_02.rom", 0x10000, 0x4000, 0x559a71ab);/* bitplanes 1,2 for bank 5,6 */
            ROM_CONTINUE(0x18000, 0x4000);
            /* bitplanes 1,2 for bank 7,8 */
            ROM_LOAD("darw_01.rom", 0x04000, 0x1000, 0x15a16973);/* bitplane 3 for bank 1,2 */
            ROM_CONTINUE(0x06000, 0x1000);
            ROM_CONTINUE(0x0c000, 0x1000);
            /* bitplane 3 for bank 3,4 */
            ROM_CONTINUE(0x0e000, 0x1000);
            ROM_CONTINUE(0x14000, 0x1000);
            /* bitplane 3 for bank 5,6 */
            ROM_CONTINUE(0x16000, 0x1000);
            ROM_CONTINUE(0x1c000, 0x1000);
            /* bitplane 3 for bank 7,8 */
            ROM_CONTINUE(0x1e000, 0x1000);

            ROM_REGION(0x18000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("darw_10.rom", 0x00000, 0x8000, 0x487a014c);/* sprites */
            ROM_LOAD("darw_11.rom", 0x08000, 0x8000, 0x548ce2d1);
            ROM_LOAD("darw_12.rom", 0x10000, 0x8000, 0xfaba5fef);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("df.12", 0x0000, 0x0100, 0x89b952ef);/* red and green component */
            ROM_LOAD("df.13", 0x0100, 0x0100, 0xd595e91d);/* blue component */

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */
            ROM_LOAD("darw_08.rom", 0x8000, 0x8000, 0x6b580d58);
            ROM_END();
        }
    };

    public static GameDriver driver_brkthru = new GameDriver("1986", "brkthru", "brkthru.java", rom_brkthru, null, machine_driver_brkthru, input_ports_brkthru, null, ROT0, "Data East USA", "Break Thru (US)");
    public static GameDriver driver_brkthruj = new GameDriver("1986", "brkthruj", "brkthru.java", rom_brkthruj, driver_brkthru, machine_driver_brkthru, input_ports_brkthru, null, ROT0, "Data East Corporation", "Kyohkoh-Toppa (Japan)");
    public static GameDriver driver_darwin = new GameDriver("1986", "darwin", "brkthru.java", rom_darwin, null, machine_driver_darwin, input_ports_darwin, null, ROT270, "Data East Corporation", "Darwin 4078 (Japan)");
}
