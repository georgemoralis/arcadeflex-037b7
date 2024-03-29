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
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
//sndhrdw imports
import static arcadeflex.v037b7.sndhrdw.scramble.*;
//sound imports
import static arcadeflex.v037b7.sound.ay8910.*;
import static arcadeflex.v037b7.sound.ay8910H.*;
import static arcadeflex.v037b7.sound.mixerH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.amidar.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class amidar {

    public static WriteHandlerPtr amidar_coina_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(0, data);
            coin_counter_w.handler(0, 0);
        }
    };

    public static WriteHandlerPtr amidar_coinb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(1, data);
            coin_counter_w.handler(1, 0);
        }
    };

    static MemoryReadAddress amidar_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x4fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM),
                new MemoryReadAddress(0x9800, 0x985f, MRA_RAM),
                new MemoryReadAddress(0xa800, 0xa800, watchdog_reset_r),
                new MemoryReadAddress(0xb000, 0xb000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xb010, 0xb010, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xb020, 0xb020, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0xb820, 0xb820, input_port_3_r), /* DSW */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x4fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x93ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9800, 0x983f, amidar_attributes_w, amidar_attributesram),
                new MemoryWriteAddress(0x9840, 0x985f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9860, 0x987f, MWA_NOP),
                new MemoryWriteAddress(0xa008, 0xa008, interrupt_enable_w),
                new MemoryWriteAddress(0xa010, 0xa010, flip_screen_x_w),
                new MemoryWriteAddress(0xa018, 0xa018, flip_screen_y_w),
                new MemoryWriteAddress(0xa030, 0xa030, amidar_coina_w),
                new MemoryWriteAddress(0xa038, 0xa038, amidar_coinb_w),
                new MemoryWriteAddress(0xb800, 0xb800, soundlatch_w),
                new MemoryWriteAddress(0xb810, 0xb810, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x83ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM),
                //	new MemoryWriteAddress( 0x9000, 0x9000, MWA_NOP ),
                //	new MemoryWriteAddress( 0x9080, 0x9080, MWA_NOP ),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x80, 0x80, AY8910_read_port_0_r),
                new IOReadPort(0x20, 0x20, AY8910_read_port_1_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x40, 0x40, AY8910_control_port_0_w),
                new IOWritePort(0x80, 0x80, AY8910_write_port_0_w),
                new IOWritePort(0x10, 0x10, AY8910_control_port_1_w),
                new IOWritePort(0x20, 0x20, AY8910_write_port_1_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_amidar = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for button 2 */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for player 2 button 2 */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000 70000");
            PORT_DIPSETTING(0x04, "50000 80000");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "Disable All Coins");
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_amidaro = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for button 2 */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for player 2 button 2 */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x02, 0x00, "Level Progression");
            PORT_DIPSETTING(0x00, "Slow");
            PORT_DIPSETTING(0x02, "Fast");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000 70000");
            PORT_DIPSETTING(0x04, "50000 80000");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "Disable All Coins");
            INPUT_PORTS_END();
        }
    };

    /* absolutely identical to amidar, the only difference is the BONUS dip switch */
    static InputPortPtr input_ports_amidarjp = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for button 2 */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for player 2 button 2 */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000 50000");
            PORT_DIPSETTING(0x04, "50000 50000");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "Disable All Coins");
            INPUT_PORTS_END();
        }
    };

    /* similar to Amidar, dip switches are different and port 3, which in Amidar */
 /* selects coins per credit, is not used. */
    static InputPortPtr input_ports_turtles = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for button 2 */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "126", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for player 2 button 2 */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 1/1 B 2/1 C 1/1");
            PORT_DIPSETTING(0x02, "A 1/2 B 1/1 C 1/2");
            PORT_DIPSETTING(0x04, "A 1/3 B 3/1 C 1/3");
            PORT_DIPSETTING(0x06, "A 1/4 B 4/1 C 1/4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /* same as Turtles, but dip switches are different. */
    static InputPortPtr input_ports_turpin = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for button 2 */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "6");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "126", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for player 2 button 2 */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /* similar to Turtles, lives are different */
    static InputPortPtr input_ports_600 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for button 2 */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "126", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably space for player 2 button 2 */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 1/1 B 2/1 C 1/1");
            PORT_DIPSETTING(0x02, "A 1/2 B 1/1 C 1/2");
            PORT_DIPSETTING(0x04, "A 1/3 B 3/1 C 1/3");
            PORT_DIPSETTING(0x06, "A 1/4 B 4/1 C 1/4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 256 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 64 * 16 * 16}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, spritelayout, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            14318000 / 8, /* 1.78975 MHz */
            new int[]{MIXERG(30, MIXER_GAIN_2x, MIXER_PAN_CENTER), MIXERG(30, MIXER_GAIN_2x, MIXER_PAN_CENTER)},
            new ReadHandlerPtr[]{soundlatch_r, null},
            new ReadHandlerPtr[]{scramble_portB_r, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_amidar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        amidar_readmem, writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 32,
            amidar_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            amidar_vh_screenrefresh,
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
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_amidar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("amidar.2c", 0x0000, 0x1000, 0xc294bf27);
            ROM_LOAD("amidar.2e", 0x1000, 0x1000, 0xe6e96826);
            ROM_LOAD("amidar.2f", 0x2000, 0x1000, 0x3656be6f);
            ROM_LOAD("amidar.2h", 0x3000, 0x1000, 0x1be170bd);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("amidar.5c", 0x0000, 0x1000, 0xc4b66ae4);
            ROM_LOAD("amidar.5d", 0x1000, 0x1000, 0x806785af);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("amidar.5f", 0x0000, 0x0800, 0x5e51e84d);
            ROM_LOAD("amidar.5h", 0x0800, 0x0800, 0x2f7f1c30);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("amidar.clr", 0x0000, 0x0020, 0xf940dcc3);
            ROM_END();
        }
    };

    static RomLoadPtr rom_amidaru = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("amidarus.2c", 0x0000, 0x1000, 0x951e0792);
            ROM_LOAD("amidarus.2e", 0x1000, 0x1000, 0xa1a3a136);
            ROM_LOAD("amidarus.2f", 0x2000, 0x1000, 0xa5121bf5);
            ROM_LOAD("amidarus.2h", 0x3000, 0x1000, 0x051d1c7f);
            ROM_LOAD("amidarus.2j", 0x4000, 0x1000, 0x351f00d5);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("amidarus.5c", 0x0000, 0x1000, 0x8ca7b750);
            ROM_LOAD("amidarus.5d", 0x1000, 0x1000, 0x9b5bdc0a);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("amidarus.5f", 0x0000, 0x0800, 0x2cfe5ede);
            ROM_LOAD("amidarus.5h", 0x0800, 0x0800, 0x57c4fd0d);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("amidar.clr", 0x0000, 0x0020, 0xf940dcc3);
            ROM_END();
        }
    };

    static RomLoadPtr rom_amidaro = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("107.2cd", 0x0000, 0x1000, 0xc52536be);
            ROM_LOAD("108.2fg", 0x1000, 0x1000, 0x38538b98);
            ROM_LOAD("109.2fg", 0x2000, 0x1000, 0x69907f0f);
            ROM_LOAD("110.2h", 0x3000, 0x1000, 0xba149a93);
            ROM_LOAD("111.2j", 0x4000, 0x1000, 0x20d01c2e);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("amidarus.5c", 0x0000, 0x1000, 0x8ca7b750);
            ROM_LOAD("amidarus.5d", 0x1000, 0x1000, 0x9b5bdc0a);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("amidarus.5f", 0x0000, 0x0800, 0x2cfe5ede);
            ROM_LOAD("113.5h", 0x0800, 0x0800, 0xbcdce168);
            /* The letter 'S' is slightly different */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("amidar.clr", 0x0000, 0x0020, 0xf940dcc3);
            ROM_END();
        }
    };

    static RomLoadPtr rom_amigo = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("2732.a1", 0x0000, 0x1000, 0x930dc856);
            ROM_LOAD("2732.a2", 0x1000, 0x1000, 0x66282ff5);
            ROM_LOAD("2732.a3", 0x2000, 0x1000, 0xe9d3dc76);
            ROM_LOAD("2732.a4", 0x3000, 0x1000, 0x4a4086c9);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("amidarus.5c", 0x0000, 0x1000, 0x8ca7b750);
            ROM_LOAD("amidarus.5d", 0x1000, 0x1000, 0x9b5bdc0a);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2716.a6", 0x0000, 0x0800, 0x2082ad0a);
            ROM_LOAD("2716.a5", 0x0800, 0x0800, 0x3029f94f);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("amidar.clr", 0x0000, 0x0020, 0xf940dcc3);
            ROM_END();
        }
    };

    static RomLoadPtr rom_turtles = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("turt_vid.2c", 0x0000, 0x1000, 0xec5e61fb);
            ROM_LOAD("turt_vid.2e", 0x1000, 0x1000, 0xfd10821e);
            ROM_LOAD("turt_vid.2f", 0x2000, 0x1000, 0xddcfc5fa);
            ROM_LOAD("turt_vid.2h", 0x3000, 0x1000, 0x9e71696c);
            ROM_LOAD("turt_vid.2j", 0x4000, 0x1000, 0xfcd49fef);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("turt_snd.5c", 0x0000, 0x1000, 0xf0c30f9a);
            ROM_LOAD("turt_snd.5d", 0x1000, 0x1000, 0xaf5fc43c);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("turt_vid.5h", 0x0000, 0x0800, 0xe5999d52);
            ROM_LOAD("turt_vid.5f", 0x0800, 0x0800, 0xc3ffd655);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("turtles.clr", 0x0000, 0x0020, 0xf3ef02dd);
            ROM_END();
        }
    };

    static RomLoadPtr rom_turpin = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("m1", 0x0000, 0x1000, 0x89177473);
            ROM_LOAD("m2", 0x1000, 0x1000, 0x4c6ca5c6);
            ROM_LOAD("m3", 0x2000, 0x1000, 0x62291652);
            ROM_LOAD("turt_vid.2h", 0x3000, 0x1000, 0x9e71696c);
            ROM_LOAD("m5", 0x4000, 0x1000, 0x7d2600f2);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("turt_snd.5c", 0x0000, 0x1000, 0xf0c30f9a);
            ROM_LOAD("turt_snd.5d", 0x1000, 0x1000, 0xaf5fc43c);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("turt_vid.5h", 0x0000, 0x0800, 0xe5999d52);
            ROM_LOAD("turt_vid.5f", 0x0800, 0x0800, 0xc3ffd655);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("turtles.clr", 0x0000, 0x0020, 0xf3ef02dd);
            ROM_END();
        }
    };

    static RomLoadPtr rom_600 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("600_vid.2c", 0x0000, 0x1000, 0x8ee090ae);
            ROM_LOAD("600_vid.2e", 0x1000, 0x1000, 0x45bfaff2);
            ROM_LOAD("600_vid.2f", 0x2000, 0x1000, 0x9f4c8ed7);
            ROM_LOAD("600_vid.2h", 0x3000, 0x1000, 0xa92ef056);
            ROM_LOAD("600_vid.2j", 0x4000, 0x1000, 0x6dadd72d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("600_snd.5c", 0x0000, 0x1000, 0x1773c68e);
            ROM_LOAD("600_snd.5d", 0x1000, 0x1000, 0xa311b998);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("600_vid.5h", 0x0000, 0x0800, 0x006c3d56);
            ROM_LOAD("600_vid.5f", 0x0800, 0x0800, 0x7dbc0426);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("turtles.clr", 0x0000, 0x0020, 0xf3ef02dd);
            ROM_END();
        }
    };

    public static GameDriver driver_amidar = new GameDriver("1981", "amidar", "amidar.java", rom_amidar, null, machine_driver_amidar, input_ports_amidarjp, null, ROT90, "Konami", "Amidar");
    public static GameDriver driver_amidaru = new GameDriver("1982", "amidaru", "amidar.java", rom_amidaru, driver_amidar, machine_driver_amidar, input_ports_amidar, null, ROT90, "Konami (Stern license)", "Amidar (Stern)");
    public static GameDriver driver_amidaro = new GameDriver("1982", "amidaro", "amidar.java", rom_amidaro, driver_amidar, machine_driver_amidar, input_ports_amidaro, null, ROT90, "Konami (Olympia license)", "Amidar (Olympia)");
    public static GameDriver driver_amigo = new GameDriver("1982", "amigo", "amidar.java", rom_amigo, driver_amidar, machine_driver_amidar, input_ports_amidar, null, ROT90, "bootleg", "Amigo");
    public static GameDriver driver_turtles = new GameDriver("1981", "turtles", "amidar.java", rom_turtles, null, machine_driver_amidar, input_ports_turtles, null, ROT90, "[Konami] (Stern license)", "Turtles");
    public static GameDriver driver_turpin = new GameDriver("1981", "turpin", "amidar.java", rom_turpin, driver_turtles, machine_driver_amidar, input_ports_turpin, null, ROT90, "[Konami] (Sega license)", "Turpin");
    public static GameDriver driver_600 = new GameDriver("1981", "600", "amidar.java", rom_600, driver_turtles, machine_driver_amidar, input_ports_600, null, ROT90, "Konami", "600");
}
