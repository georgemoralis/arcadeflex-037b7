/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.cstring.*;
import static arcadeflex.common.libc.expressions.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.inputH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;
//sound imports
import static arcadeflex.v037b7.sound.ay8910.*;
import static arcadeflex.v037b7.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.gameplan.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class gameplan {

    static int gameplan_current_port;

    public static WriteHandlerPtr gameplan_port_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x00:
                    switch (data) {
                        case 0x01:
                            gameplan_current_port = 0;
                            break;
                        case 0x02:
                            gameplan_current_port = 1;
                            break;
                        case 0x04:
                            gameplan_current_port = 2;
                            break;
                        case 0x08:
                            gameplan_current_port = 3;
                            break;
                        case 0x80:
                            gameplan_current_port = 4;
                            break;
                        case 0x40:
                            gameplan_current_port = 5;
                            break;

                        default:
                            return;
                    }

                    break;

                case 0x02:
                    break;

                case 0x03:
                    break;

                case 0x0c:
                    if (data == 0xec || data == 0xcc) {
                    } else {
                        logerror("  VIA 2: unusual Peripheral Control Register value 0x%02x for VIA 2\n", data);
                    }
                    break;

                default:
                    logerror("  VIA 2: unexpected register written to in VIA 2: %02x . %02x\n", data, offset);
                    break;
            }
        }
    };

    public static ReadHandlerPtr gameplan_port_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(gameplan_current_port);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x032d, 0x03d8, MRA_RAM), /* note: 300-32c and 3d9-3ff is
									  * written but never read?
									  * (write by code at e1df and e1e9,
									  * 32d is read by e258)*/
                new MemoryReadAddress(0x2000, 0x200f, gameplan_video_r),
                new MemoryReadAddress(0x2801, 0x2801, gameplan_port_r),
                new MemoryReadAddress(0x3000, 0x300f, gameplan_sound_r),
                new MemoryReadAddress(0x9000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x200f, gameplan_video_w), /* VIA 1 */
                new MemoryWriteAddress(0x2800, 0x280f, gameplan_port_select_w), /* VIA 2 */
                new MemoryWriteAddress(0x3000, 0x300f, gameplan_sound_w), /* VIA 3 */
                new MemoryWriteAddress(0x9000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_snd[]
            = {
                new MemoryReadAddress(0x0000, 0x0026, MRA_RAM),
                new MemoryReadAddress(0x01f6, 0x01ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x080f, gameplan_via5_r),
                new MemoryReadAddress(0xa001, 0xa001, soundlatch_r), /* AY-3-8910 */
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_snd[]
            = {
                new MemoryWriteAddress(0x0000, 0x0026, MWA_RAM),
                new MemoryWriteAddress(0x01f6, 0x01ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x080f, gameplan_via5_w),
                new MemoryWriteAddress(0xa000, 0xa000, AY8910_control_port_0_w), /* AY-3-8910 */
                new MemoryWriteAddress(0xa002, 0xa002, AY8910_write_port_0_w), /* AY-3-8910 */
                new MemoryWriteAddress(0xf800, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    public static InterruptPtr gameplan_interrupt = new InterruptPtr() {
        public int handler() {
            return 1;
        }
    };

    static InputPortPtr input_ports_kaos = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - from "TEST NO.7 - status locator - coin-door" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "Do Tests", KEYCODE_F1, IP_JOY_NONE);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Select Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* IN1 - from "TEST NO.7 - status locator - start sws." */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 - from "TEST NO.8 - status locator - player no.1" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Jump", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1, "P1 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1, "P1 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */

            PORT_START();
            /* IN3 - from "TEST NO.8 - status locator - player no.2" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2, "P2 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "P2 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */

            PORT_START();
            /* IN4 - from "TEST NO.6 - dip switch A" */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
            /* . 039F */
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            //	PORT_DIPSETTING(   0x0e, DEF_STR( "1C_1C") );
            PORT_DIPSETTING(0x0d, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_8C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_9C"));
            PORT_DIPSETTING(0x05, "1 Coin/10 Credits");
            PORT_DIPSETTING(0x04, "1 Coin/11 Credits");
            PORT_DIPSETTING(0x03, "1 Coin/12 Credits");
            PORT_DIPSETTING(0x02, "1 Coin/13 Credits");
            PORT_DIPSETTING(0x01, "1 Coin/14 Credits");

            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            /* . 039A */
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x60, 0x20, DEF_STR("Unknown"));
            /* . 039C */
            PORT_DIPSETTING(0x60, "1");
            PORT_DIPSETTING(0x40, "2");
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x00, "4");

            PORT_DIPNAME(0x80, 0x80, DEF_STR("Free_Play"));
            /* . 039D */
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* IN5 - from "TEST NO.6 - dip switch B" */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "4");

            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0c, "1");
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "4");

            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_killcom = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - from "TEST NO.7 - status locator - coin-door" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "Do Tests", KEYCODE_F1, IP_JOY_NONE);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Select Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* IN1 - from "TEST NO.7 - status locator - start sws." */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 - from "TEST NO.8 - status locator - player no.1" */
            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1, "P1 Hyperspace", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1, "P1 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1, "P1 Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1, "P1 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1, "P1 Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT);

            PORT_START();
            /* IN3 - from "TEST NO.8 - status locator - player no.2" */
            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2, "P2 Hyperspace", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2, "P2 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2, "P2 Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "P2 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2, "P2 Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT);

            PORT_START();
            /* IN4 - from "TEST NO.6 - dip switch A" */

            PORT_DIPNAME(0x03, 0x03, "Coinage P1/P2");
            PORT_DIPSETTING(0x03, "1 Credit/2 Credits");
            PORT_DIPSETTING(0x02, "2 Credits/3 Credits");
            PORT_DIPSETTING(0x01, "2 Credits/4 Credits");
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_DIPNAME(0x08, 0x08, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPSETTING(0x08, "5");

            PORT_DIPNAME(0xc0, 0xc0, "Reaction");
            PORT_DIPSETTING(0xc0, "Slowest");
            PORT_DIPSETTING(0x80, "Slow");
            PORT_DIPSETTING(0x40, "Fast");
            PORT_DIPSETTING(0x00, "Fastest");

            PORT_START();
            /* IN5 - from "TEST NO.6 - dip switch B" */

            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_megatack = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - from "TEST NO.7 - status locator - coin-door" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "Do Tests", KEYCODE_F1, IP_JOY_NONE);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Select Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* IN1 - from "TEST NO.7 - status locator - start sws." */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 - from "TEST NO.8 - status locator - player no.1" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Fire", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            /* PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
   PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT )*/
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1, "P1 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1, "P1 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */

            PORT_START();
            /* IN3 - from "TEST NO.8 - status locator - player no.2" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Fire", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            /* PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
   PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT )*/
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2, "P2 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "P2 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */

            PORT_START();
            /* IN4 - from "TEST NO.6 - dip switch A" */
            PORT_DIPNAME(0x03, 0x03, "Coinage P1/P2");
            PORT_DIPSETTING(0x03, "1 Credit/2 Credits");
            PORT_DIPSETTING(0x02, "2 Credits/3 Credits");
            PORT_DIPSETTING(0x01, "2 Credits/4 Credits");
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_DIPNAME(0x08, 0x08, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x00, "4");

            PORT_START();
            /* IN5 - from "TEST NO.6 - dip switch B" */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x07, "20000");
            PORT_DIPSETTING(0x06, "30000");
            PORT_DIPSETTING(0x05, "40000");
            PORT_DIPSETTING(0x04, "50000");
            PORT_DIPSETTING(0x03, "60000");
            PORT_DIPSETTING(0x02, "70000");
            PORT_DIPSETTING(0x01, "80000");
            PORT_DIPSETTING(0x00, "90000");

            PORT_DIPNAME(0x10, 0x10, "Monitor View");
            PORT_DIPSETTING(0x10, "Direct");
            PORT_DIPSETTING(0x00, "Mirror");

            PORT_DIPNAME(0x20, 0x20, "Monitor Orientation");
            PORT_DIPSETTING(0x20, "Horizontal");
            PORT_DIPSETTING(0x00, "Vertical");

            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_challeng = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - from "TEST NO.7 - status locator - coin-door" */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "Do Tests", KEYCODE_F1, IP_JOY_NONE);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Select Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 - from "TEST NO.7 - status locator - start sws." */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 - from "TEST NO.8 - status locator - player no.1" */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Warp", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Fire", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Bomb", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1, "P1 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1, "P1 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */

            PORT_START();
            /* IN3 - from "TEST NO.8 - status locator - player no.2" */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Warp", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Fire", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Bomb", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2, "P2 Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */
            PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "P2 Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* unused */

            PORT_START();
            /* IN4 - from "TEST NO.6 - dip switch A" */

            PORT_DIPNAME(0x03, 0x03, "Coinage P1/P2");
            PORT_DIPSETTING(0x03, "1 Credit/2 Credits");
            PORT_DIPSETTING(0x02, "2 Credits/3 Credits");
            PORT_DIPSETTING(0x01, "2 Credits/4 Credits");
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Lives"));
            PORT_DIPSETTING(0xc0, "3");
            PORT_DIPSETTING(0x80, "4");
            PORT_DIPSETTING(0x40, "5");
            PORT_DIPSETTING(0x00, "6");

            PORT_START();
            /* IN5 - from "TEST NO.6 - dip switch B" */
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x07, "40000");
            PORT_DIPSETTING(0x06, "50000");
            PORT_DIPSETTING(0x05, "60000");
            PORT_DIPSETTING(0x04, "70000");
            PORT_DIPSETTING(0x03, "80000");
            PORT_DIPSETTING(0x02, "90000");
            PORT_DIPNAME(0x10, 0x10, "Monitor View");
            PORT_DIPSETTING(0x10, "Direct");
            PORT_DIPSETTING(0x00, "Mirror");
            PORT_DIPNAME(0x20, 0x20, "Monitor Orientation");
            PORT_DIPSETTING(0x20, "Horizontal");
            PORT_DIPSETTING(0x00, "Vertical");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static char palette[]
            = {
                0xff, 0xff, 0xff, /* 0 WHITE   */
                0x20, 0xff, 0xff, /* 1 CYAN    */
                0xff, 0x20, 0xff, /* 2 MAGENTA */
                0x20, 0x20, 0xFF, /* 3 BLUE    */
                0xff, 0xff, 0x20, /* 4 YELLOW  */
                0x20, 0xff, 0x20, /* 5 GREEN   */
                0xff, 0x20, 0x20, /* 6 RED     */
                0x00, 0x00, 0x00, /* 7 BLACK   */};

    public static VhConvertColorPromPtr init_palette = new VhConvertColorPromPtr() {
        public void handler(char[] game_palette, char[] game, UBytePtr color_prom) {
            memcpy(game_palette, palette, sizeof(palette));
        }
    };

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{50},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_gameplan = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{ /* MachineCPU */
                new MachineCPU(
                        CPU_M6502,
                        3579000 / 4, /* 3.579 / 4 MHz */
                        readmem, writemem, null, null,
                        gameplan_interrupt, 1 /* 1 interrupt per frame */
                ),
                new MachineCPU(
                        CPU_M6502 | CPU_AUDIO_CPU,
                        3579000 / 4, /* 3.579 / 4 MHz */
                        readmem_snd, writemem_snd, null, null,
                        gameplan_interrupt, 1
                ),},
            57, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* CPU slices per frame */
            null, /* init_machine */
            /* video hardware */
            32 * 8, 32 * 8, /* screen_width, height */
            new rectangle(0, 32 * 8 - 1, 0, 32 * 8 - 1), /* visible_area */
            null,
            palette.length / 3, 0,
            init_palette,
            VIDEO_TYPE_RASTER, null,
            gameplan_vh_start,
            generic_bitmapped_vh_stop,
            generic_bitmapped_vh_screenrefresh,
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
    /*
	the manuals for Megattack and the schematics for Kaos are up
	on spies now. I took a quick look at the rom mapping for kaos
	and it looks like the roms are split this way:
	
	9000 G2 bot 2k
	9800 J2 bot 2k
	A000 J1 bot 2k
	A800 G1 bot 2k
	B000 F1 bot 2k
	B800 E1 bot 2k
	
	D000 G2 top 2k
	D800 J2 top 2k
	E000 J1 top 2k
	E800 G1 top 2k
	F000 F1 top 2k
	F800 E1 top 2k
	
	there are three 6522 VIAs, at 2000, 2800, and 3000
     */
    static RomLoadPtr rom_kaos = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("kaosab.g2", 0x9000, 0x0800, 0xb23d858f);
            ROM_CONTINUE(0xd000, 0x0800);
            ROM_LOAD("kaosab.j2", 0x9800, 0x0800, 0x4861e5dc);
            ROM_CONTINUE(0xd800, 0x0800);
            ROM_LOAD("kaosab.j1", 0xa000, 0x0800, 0xe055db3f);
            ROM_CONTINUE(0xe000, 0x0800);
            ROM_LOAD("kaosab.g1", 0xa800, 0x0800, 0x35d7c467);
            ROM_CONTINUE(0xe800, 0x0800);
            ROM_LOAD("kaosab.f1", 0xb000, 0x0800, 0x995b9260);
            ROM_CONTINUE(0xf000, 0x0800);
            ROM_LOAD("kaosab.e1", 0xb800, 0x0800, 0x3da5202a);
            ROM_CONTINUE(0xf800, 0x0800);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("kaossnd.e1", 0xf800, 0x800, 0xab23d52a);
            ROM_END();
        }
    };

    static RomLoadPtr rom_killcom = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("killcom.e2", 0xc000, 0x800, 0xa01cbb9a);
            ROM_LOAD("killcom.f2", 0xc800, 0x800, 0xbb3b4a93);
            ROM_LOAD("killcom.g2", 0xd000, 0x800, 0x86ec68b2);
            ROM_LOAD("killcom.j2", 0xd800, 0x800, 0x28d8c6a1);
            ROM_LOAD("killcom.j1", 0xe000, 0x800, 0x33ef5ac5);
            ROM_LOAD("killcom.g1", 0xe800, 0x800, 0x49cb13e2);
            ROM_LOAD("killcom.f1", 0xf000, 0x800, 0xef652762);
            ROM_LOAD("killcom.e1", 0xf800, 0x800, 0xbc19dcb7);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("killsnd.e1", 0xf800, 0x800, 0x77d4890d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_megatack = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("megattac.e2", 0xc000, 0x800, 0x33fa5104);
            ROM_LOAD("megattac.f2", 0xc800, 0x800, 0xaf5e96b1);
            ROM_LOAD("megattac.g2", 0xd000, 0x800, 0x670103ea);
            ROM_LOAD("megattac.j2", 0xd800, 0x800, 0x4573b798);
            ROM_LOAD("megattac.j1", 0xe000, 0x800, 0x3b1d01a1);
            ROM_LOAD("megattac.g1", 0xe800, 0x800, 0xeed75ef4);
            ROM_LOAD("megattac.f1", 0xf000, 0x800, 0xc93a8ed4);
            ROM_LOAD("megattac.e1", 0xf800, 0x800, 0xd9996b9f);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("megatsnd.e1", 0xf800, 0x800, 0x0c186bdb);
            ROM_END();
        }
    };

    static RomLoadPtr rom_challeng = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("chall.6", 0xa000, 0x1000, 0xb30fe7f5);
            ROM_LOAD("chall.5", 0xb000, 0x1000, 0x34c6a88e);
            ROM_LOAD("chall.4", 0xc000, 0x1000, 0x0ddc18ef);
            ROM_LOAD("chall.3", 0xd000, 0x1000, 0x6ce03312);
            ROM_LOAD("chall.2", 0xe000, 0x1000, 0x948912ad);
            ROM_LOAD("chall.1", 0xf000, 0x1000, 0x7c71a9dc);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("chall.snd", 0xf800, 0x800, 0x1b2bffd2);
            ROM_END();
        }
    };

    public static GameDriver driver_kaos = new GameDriver("1981", "kaos", "gameplan.java", rom_kaos, null, machine_driver_gameplan, input_ports_kaos, null, ROT270, "GamePlan", "Kaos");
    public static GameDriver driver_killcom = new GameDriver("1980", "killcom", "gameplan.java", rom_killcom, null, machine_driver_gameplan, input_ports_killcom, null, ROT0, "GamePlan (Centuri license)", "Killer Comet");
    public static GameDriver driver_megatack = new GameDriver("1980", "megatack", "gameplan.java", rom_megatack, null, machine_driver_gameplan, input_ports_megatack, null, ROT0, "GamePlan (Centuri license)", "MegaTack");
    public static GameDriver driver_challeng = new GameDriver("1980", "challeng", "gameplan.java", rom_challeng, null, machine_driver_gameplan, input_ports_challeng, null, ROT0, "GamePlan (Centuri license)", "Challenger");
}
