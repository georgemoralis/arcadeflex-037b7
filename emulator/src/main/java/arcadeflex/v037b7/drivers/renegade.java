/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.libc.expressions.*;
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
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;
//sound imports
import static arcadeflex.v037b7.sound._3526intf.*;
import static arcadeflex.v037b7.sound._3812intfH.*;
import static arcadeflex.v037b7.sound.adpcmH.*;
import static arcadeflex.v037b7.sound.adpcm.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;
import static arcadeflex.v037b7.vidhrdw.renegade.*;

public class renegade {

    /**
     * *****************************************************************************************
     */
    public static WriteHandlerPtr adpcm_play_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data -= 0x2c;
            if (data >= 0) {
                ADPCM_play(0, 0x2000 * data, 0x2000 * 2);
            }
        }
    };

    public static WriteHandlerPtr sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, M6809_INT_IRQ);
        }
    };

    /**
     * *****************************************************************************************
     */
    /*	MCU Simulation
	**
	**	Renegade and Nekketsu Kouha Kunio Kun MCU behaviors are identical,
	**	except for the initial MCU status byte, and command encryption table.
     */
    static int mcu_type;
    static UBytePtr mcu_encrypt_table;
    static int mcu_encrypt_table_len;

    static char renegade_xor_table[] = {
        0x8A, 0x48, 0x98, 0x48, 0xA9, 0x00, 0x85, 0x14, 0x85, 0x15, 0xA5, 0x11, 0x05, 0x10, 0xF0, 0x21,
        0x46, 0x11, 0x66, 0x10, 0x90, 0x0F, 0x18, 0xA5, 0x14, 0x65, 0x12, 0x85, 0x14, 0xA5, 0x15, 0x65,
        0x13, 0x85, 0x15, 0xB0, 0x06, 0x06, 0x12, 0x26, 0x13, 0x90, 0xDF, 0x68, 0xA8, 0x68, 0xAA, 0x38,
        0x60, 0x68, 0xA8, 0x68, 0xAA, 0x18, 0x60
    };

    static char kuniokun_xor_table[] = {
        0x48, 0x8a, 0x48, 0xa5, 0x01, 0x48, 0xa9, 0x00, 0x85, 0x01, 0xa2, 0x10, 0x26, 0x10, 0x26, 0x11,
        0x26, 0x01, 0xa5, 0x01, 0xc5, 0x00, 0x90, 0x04, 0xe5, 0x00, 0x85, 0x01, 0x26, 0x10, 0x26, 0x11,
        0xca, 0xd0, 0xed, 0x68, 0x85, 0x01, 0x68, 0xaa, 0x68, 0x60
    };

    public static InitDriverPtr init_kuniokun = new InitDriverPtr() {
        public void handler() {
            mcu_type = 0x85;
            mcu_encrypt_table = new UBytePtr(kuniokun_xor_table);
            mcu_encrypt_table_len = 0x2a;
        }
    };
    public static InitDriverPtr init_renegade = new InitDriverPtr() {
        public void handler() {
            mcu_type = 0xda;
            mcu_encrypt_table = new UBytePtr(renegade_xor_table);
            mcu_encrypt_table_len = 0x37;
        }
    };

    static int MCU_BUFFER_MAX = 6;
    static char[] u8_mcu_buffer = new char[MCU_BUFFER_MAX];
    static int mcu_input_size;
    static int mcu_output_byte;
    static int mcu_key;

    public static ReadHandlerPtr mcu_reset_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            mcu_key = -1;
            mcu_input_size = 0;
            mcu_output_byte = 0;
            return 0;
        }
    };

    public static WriteHandlerPtr mcu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            mcu_output_byte = 0;

            if (mcu_key < 0) {
                mcu_key = 0;
                mcu_input_size = 1;
                u8_mcu_buffer[0] = (char) (data & 0xFF);
            } else {
                data ^= mcu_encrypt_table.read(mcu_key);
                if (++mcu_key == mcu_encrypt_table_len) {
                    mcu_key = 0;
                }
                if (mcu_input_size < MCU_BUFFER_MAX) {
                    u8_mcu_buffer[mcu_input_size++] = (char) (data & 0xFF);
                }
            }
        }
    };

    static void mcu_process_command() {
        mcu_input_size = 0;
        mcu_output_byte = 0;

        switch (u8_mcu_buffer[0]) {
            case 0x10:
                u8_mcu_buffer[0] = (char) (mcu_type & 0xFF);
                break;

            case 0x26: /* sound code . sound command */ {
                int sound_code = u8_mcu_buffer[1];
                char sound_command_table[] = {
                    0xa0, 0xa1, 0xa2, 0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x8b, 0x8c,
                    0x8d, 0x8e, 0x8f, 0x97, 0x96, 0x9b, 0x9a, 0x95, 0x9e, 0x98, 0x90, 0x93, 0x9d, 0x9c, 0xa3, 0x91,
                    0x9f, 0x99, 0xa6, 0xae, 0x94, 0xa5, 0xa4, 0xa7, 0x92, 0xab, 0xac, 0xb0, 0xb1, 0xb2, 0xb3, 0xb4,
                    0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xbb, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x20,
                    0x50, 0x50, 0x90, 0x30, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x80, 0xa0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x20, 0x00, 0x00, 0x10, 0x10, 0x00, 0x00, 0x90, 0x30, 0x30, 0x30, 0xb0, 0xb0, 0xb0, 0xb0, 0xf0,
                    0xf0, 0xf0, 0xf0, 0xd0, 0xf0, 0x00, 0x00, 0x00, 0x00, 0x10, 0x10, 0x50, 0x30, 0xb0, 0xb0, 0xf0,
                    0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10,
                    0x10, 0x10, 0x30, 0x30, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f,
                    0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x8f, 0x8f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f,
                    0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0xff, 0xff, 0xff, 0xef, 0xef, 0xcf, 0x8f, 0x8f, 0x0f, 0x0f, 0x0f
                };
                u8_mcu_buffer[0] = 1;
                u8_mcu_buffer[1] = sound_command_table[sound_code];
            }
            break;

            case 0x33: /* joy bits . joy dir */ {
                int joy_bits = u8_mcu_buffer[2];
                char joy_table[] = {
                    0, 3, 7, 0, 1, 2, 8, 0, 5, 4, 6, 0, 0, 0, 0, 0
                };
                u8_mcu_buffer[0] = 1;
                u8_mcu_buffer[1] = joy_table[joy_bits & 0xf];
            }
            break;

            case 0x44: /* 0x44,0xff,DSW2,stage# . difficulty */ {
                int difficulty = u8_mcu_buffer[2] & 0x3;
                int stage = u8_mcu_buffer[3];
                char difficulty_table[] = {5, 3, 1, 2};
                int result = difficulty_table[difficulty];
                if (stage == 0) {
                    result--;
                }
                result += stage / 4;
                u8_mcu_buffer[0] = 1;
                u8_mcu_buffer[1] = (char) (result & 0xFF);
            }

            case 0x55: /* 0x55,0x00,0x00,0x00,DSW2 . timer */ {
                int difficulty = u8_mcu_buffer[4] & 0x3;
                char table[] = {
                    0x4001, 0x5001, 0x1502, 0x0002
                };

                u8_mcu_buffer[0] = 3;
                u8_mcu_buffer[2] = (char) (table[difficulty] >>> 8);
                u8_mcu_buffer[3] = (char) (table[difficulty] & 0xff);
            }
            break;

            case 0x41: {
                /* 0x41,0x00,0x00,stage# . ? */
                //			int stage = mcu_buffer[3];
                u8_mcu_buffer[0] = 2;
                u8_mcu_buffer[1] = 0x20;
                u8_mcu_buffer[2] = 0x78;
            }
            break;

            case 0x40: /* 0x40,0x00,difficulty,enemy_type . enemy health */ {
                int difficulty = u8_mcu_buffer[2];
                int enemy_type = u8_mcu_buffer[3];
                int health;

                if (enemy_type <= 4 || (enemy_type & 1) == 0) {
                    health = 0x18 + difficulty * 8;
                } else {
                    health = 0x06 + difficulty * 2;
                }
                logerror("e_type:0x%02x diff:0x%02x . 0x%02x\n", enemy_type, difficulty, health);
                u8_mcu_buffer[0] = 1;
                u8_mcu_buffer[1] = (char) (health & 0xFF);
            }
            break;

            case 0x42: /* 0x42,0x00,stage#,character# . enemy_type */ {
                int stage = u8_mcu_buffer[2] & 0x3;
                int indx = u8_mcu_buffer[3];
                int enemy_type = 0;

                if (indx == 0) {
                    enemy_type = 1 + stage;
                    /* boss */
                } else {
                    switch (stage) {
                        case 0x00:
                            if (indx <= 2) {
                                enemy_type = 0x06;
                            } else {
                                enemy_type = 0x05;
                            }
                            break;

                        case 0x01:
                            if (indx <= 2) {
                                enemy_type = 0x0a;
                            } else {
                                enemy_type = 0x09;
                            }
                            break;

                        case 0x02:
                            if (indx <= 3) {
                                enemy_type = 0x0e;
                            } else {
                                enemy_type = 0xd;
                            }
                            break;

                        case 0x03:
                            enemy_type = 0x12;
                            break;
                    }
                }
                u8_mcu_buffer[0] = 1;
                u8_mcu_buffer[1] = (char) (enemy_type & 0xFF);
            }
            break;

            default:
                logerror("unknown MCU command: %02x\n", u8_mcu_buffer[0]);
                break;
        }
    }

    public static ReadHandlerPtr mcu_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int result = 1;

            if (mcu_input_size != 0) {
                mcu_process_command();
            }

            if (mcu_output_byte < MCU_BUFFER_MAX) {
                result = u8_mcu_buffer[mcu_output_byte++];
            }

            return result;
        }
    };

    /**
     * *****************************************************************************************
     */
    static int bank;

    public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 1) != bank) {
                UBytePtr RAM = memory_region(REGION_CPU1);
                bank = data & 1;
                cpu_setbank(1, new UBytePtr(RAM, bank != 0 ? 0x10000 : 0x4000));
            }
        }
    };
    static int count;
    public static InterruptPtr renegade_interrupt = new InterruptPtr() {
        public int handler() {
            /*
		static int coin;
		int port = readinputport(1) & 0xc0;
		if (port != 0xc0 ){
			if (coin == 0){
				coin = 1;
				return interrupt();
			}
		}
		else coin = 0;
             */

            count = NOT(count);
            if (count != 0) {
                return nmi_interrupt.handler();
            }
            return interrupt.handler();
        }
    };

    /**
     * *****************************************************************************************
     */
    static MemoryReadAddress main_readmem[] = {
        new MemoryReadAddress(0x0000, 0x37ff, MRA_RAM),
        new MemoryReadAddress(0x3800, 0x3800, input_port_0_r), /* Player#1 controls; P1,P2 start */
        new MemoryReadAddress(0x3801, 0x3801, input_port_1_r), /* Player#2 controls; coin triggers */
        new MemoryReadAddress(0x3802, 0x3802, input_port_2_r), /* DIP2 ; various IO ports */
        new MemoryReadAddress(0x3803, 0x3803, input_port_3_r), /* DIP1 */
        new MemoryReadAddress(0x3804, 0x3804, mcu_r),
        new MemoryReadAddress(0x3805, 0x3805, mcu_reset_r),
        new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
        new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress main_writemem[] = {
        new MemoryWriteAddress(0x0000, 0x17ff, MWA_RAM),
        new MemoryWriteAddress(0x1800, 0x1fff, renegade_textram_w, renegade_textram),
        new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, spriteram),
        new MemoryWriteAddress(0x2800, 0x2fff, renegade_videoram_w, videoram),
        new MemoryWriteAddress(0x3000, 0x30ff, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
        new MemoryWriteAddress(0x3100, 0x31ff, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
        new MemoryWriteAddress(0x3800, 0x3800, renegade_scroll0_w),
        new MemoryWriteAddress(0x3801, 0x3801, renegade_scroll1_w),
        new MemoryWriteAddress(0x3802, 0x3802, sound_w),
        new MemoryWriteAddress(0x3803, 0x3803, renegade_flipscreen_w),
        new MemoryWriteAddress(0x3804, 0x3804, mcu_w),
        new MemoryWriteAddress(0x3805, 0x3805, bankswitch_w),
        new MemoryWriteAddress(0x3806, 0x3806, MWA_NOP), /* watchdog? */
        new MemoryWriteAddress(0x3807, 0x3807, coin_counter_w),
        new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
        new MemoryWriteAddress(-1)
    };

    static MemoryReadAddress sound_readmem[] = {
        new MemoryReadAddress(0x0000, 0x0fff, MRA_RAM),
        new MemoryReadAddress(0x1000, 0x1000, soundlatch_r),
        new MemoryReadAddress(0x2801, 0x2801, YM3526_status_port_0_r),
        new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress sound_writemem[] = {
        new MemoryWriteAddress(0x0000, 0x0fff, MWA_RAM),
        new MemoryWriteAddress(0x1800, 0x1800, MWA_NOP), // this gets written the same values as 0x2000
        new MemoryWriteAddress(0x2000, 0x2000, adpcm_play_w),
        new MemoryWriteAddress(0x2800, 0x2800, YM3526_control_port_0_w),
        new MemoryWriteAddress(0x2801, 0x2801, YM3526_write_port_0_w),
        new MemoryWriteAddress(0x3000, 0x3000, MWA_NOP), /* adpcm related? stereo pan? */
        new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
        new MemoryWriteAddress(-1)
    };

    static InputPortPtr input_ports_renegade = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);/* attack left */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);/* jump */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* DIP2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON3);/* attack right */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            /* 68705 status */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);/* 68705 status */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_VBLANK);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DIP1 */
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
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Lives"));
            PORT_DIPSETTING(0x10, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPNAME(0x20, 0x20, "Bonus");
            PORT_DIPSETTING(0x20, "30k");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8x8 characters */
            1024, /* 1024 characters */
            3, /* bits per pixel */
            new int[]{2, 4, 6}, /* plane offsets; bit 0 is always clear */
            new int[]{1, 0, 65, 64, 129, 128, 193, 192}, /* x offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8}, /* y offsets */
            32 * 8 /* offset to next character */
    );

    static GfxLayout tileslayout1 = new GfxLayout(
            16, 16, /* tile size */
            256, /* number of tiles */
            3, /* bits per pixel */
            /* plane offsets */
            new int[]{4, 0x8000 * 8 + 0, 0x8000 * 8 + 4},
            /* x offsets */
            new int[]{3, 2, 1, 0, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0,
                32 * 8 + 3, 32 * 8 + 2, 32 * 8 + 1, 32 * 8 + 0, 48 * 8 + 3, 48 * 8 + 2, 48 * 8 + 1, 48 * 8 + 0},
            /* y offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            64 * 8 /* offset to next tile */
    );

    static GfxLayout tileslayout2 = new GfxLayout(
            16, 16, /* tile size */
            256, /* number of tiles */
            3, /* bits per pixel */
            /* plane offsets */
            new int[]{0, 0xC000 * 8 + 0, 0xC000 * 8 + 4},
            /* x offsets */
            new int[]{3, 2, 1, 0, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0,
                32 * 8 + 3, 32 * 8 + 2, 32 * 8 + 1, 32 * 8 + 0, 48 * 8 + 3, 48 * 8 + 2, 48 * 8 + 1, 48 * 8 + 0},
            /* y offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            64 * 8 /* offset to next tile */
    );

    static GfxLayout tileslayout3 = new GfxLayout(
            16, 16, /* tile size */
            256, /* number of tiles */
            3, /* bits per pixel */
            /* plane offsets */
            new int[]{0x4000 * 8 + 4, 0x10000 * 8 + 0, 0x10000 * 8 + 4},
            /* x offsets */
            new int[]{3, 2, 1, 0, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0,
                32 * 8 + 3, 32 * 8 + 2, 32 * 8 + 1, 32 * 8 + 0, 48 * 8 + 3, 48 * 8 + 2, 48 * 8 + 1, 48 * 8 + 0},
            /* y offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            64 * 8 /* offset to next tile */
    );

    static GfxLayout tileslayout4 = new GfxLayout(
            16, 16, /* tile size */
            256, /* number of tiles */
            3, /* bits per pixel */
            /* plane offsets */
            new int[]{0x4000 * 8 + 0, 0x14000 * 8 + 0, 0x14000 * 8 + 4},
            /* x offsets */
            new int[]{3, 2, 1, 0, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0,
                32 * 8 + 3, 32 * 8 + 2, 32 * 8 + 1, 32 * 8 + 0, 48 * 8 + 3, 48 * 8 + 2, 48 * 8 + 1, 48 * 8 + 0},
            /* y offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            64 * 8 /* offset to next tile */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                /* 8x8 text, 8 colors */
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 0, 4), /* colors   0- 32 */
                /* 16x16 background tiles, 8 colors */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tileslayout1, 192, 8), /* colors 192-255 */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tileslayout2, 192, 8),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tileslayout3, 192, 8),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tileslayout4, 192, 8),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, tileslayout1, 192, 8),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, tileslayout2, 192, 8),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, tileslayout3, 192, 8),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, tileslayout4, 192, 8),
                /* 16x16 sprites, 8 colors */
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tileslayout1, 128, 4), /* colors 128-159 */
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tileslayout2, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tileslayout3, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tileslayout4, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, tileslayout1, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, tileslayout2, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, tileslayout3, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, tileslayout4, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x30000, tileslayout1, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x30000, tileslayout2, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x30000, tileslayout3, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x30000, tileslayout4, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x48000, tileslayout1, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x48000, tileslayout2, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x48000, tileslayout3, 128, 4),
                new GfxDecodeInfo(REGION_GFX3, 0x48000, tileslayout4, 128, 4),
                new GfxDecodeInfo(-1)
            };

    /* handler called by the 3526 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
            cpu_set_irq_line(1, M6809_FIRQ_LINE, linestate);
        }
    };

    static YM3526interface ym3526_interface = new YM3526interface(
            1, /* 1 chip (no more supported) */
            3000000, /* 3 MHz ? (hand tuned) */
            new int[]{50}, /* volume */
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static ADPCMinterface adpcm_interface = new ADPCMinterface(
            1, /* 1 channel */
            8000, /* 8000Hz playback */
            REGION_SOUND1, /* memory region */
            new int[]{100} /* volume */
    );

    static MachineDriver machine_driver_renegade = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz? */
                        main_readmem, main_writemem, null, null,
                        renegade_interrupt, 2
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        1500000, /* ? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* FIRQs are caused by the YM3526 */
                /* IRQs are caused by the main CPU */
                )
            },
            60,
            DEFAULT_REAL_60HZ_VBLANK_DURATION * 2,
            1, /* cpu slices */
            null, /* init machine */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 0, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            renegade_vh_start, null,
            renegade_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3526,
                        ym3526_interface
                ),
                new MachineSound(
                        SOUND_ADPCM,
                        adpcm_interface
                )
            }
    );

    static RomLoadPtr rom_renegade = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1);/* 64k for code + bank switched ROM */
            ROM_LOAD("nb-5.bin", 0x08000, 0x8000, 0xba683ddf);
            ROM_LOAD("na-5.bin", 0x04000, 0x4000, 0xde7e7df4);
            ROM_CONTINUE(0x10000, 0x4000);

            ROM_REGION(0x10000, REGION_CPU2);/* audio CPU (M6809) */
            ROM_LOAD("n0-5.bin", 0x8000, 0x8000, 0x3587de3b);

            ROM_REGION(0x10000, REGION_CPU3);/* mcu (missing) */
            ROM_LOAD("mcu", 0x8000, 0x8000, 0x00000000);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nc-5.bin", 0x0000, 0x8000, 0x9adfaa5d);/* characters */

            ROM_REGION(0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("n1-5.bin", 0x00000, 0x8000, 0x4a9f47f3);/* tiles */
            ROM_LOAD("n6-5.bin", 0x08000, 0x8000, 0xd62a0aa8);
            ROM_LOAD("n7-5.bin", 0x10000, 0x8000, 0x7ca5a532);
            ROM_LOAD("n2-5.bin", 0x18000, 0x8000, 0x8d2e7982);
            ROM_LOAD("n8-5.bin", 0x20000, 0x8000, 0x0dba31d3);
            ROM_LOAD("n9-5.bin", 0x28000, 0x8000, 0x5b621b6a);

            ROM_REGION(0x60000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nh-5.bin", 0x00000, 0x8000, 0xdcd7857c);/* sprites */
            ROM_LOAD("nd-5.bin", 0x08000, 0x8000, 0x2de1717c);
            ROM_LOAD("nj-5.bin", 0x10000, 0x8000, 0x0f96a18e);
            ROM_LOAD("nn-5.bin", 0x18000, 0x8000, 0x1bf15787);
            ROM_LOAD("ne-5.bin", 0x20000, 0x8000, 0x924c7388);
            ROM_LOAD("nk-5.bin", 0x28000, 0x8000, 0x69499a94);
            ROM_LOAD("ni-5.bin", 0x30000, 0x8000, 0x6f597ed2);
            ROM_LOAD("nf-5.bin", 0x38000, 0x8000, 0x0efc8d45);
            ROM_LOAD("nl-5.bin", 0x40000, 0x8000, 0x14778336);
            ROM_LOAD("no-5.bin", 0x48000, 0x8000, 0x147dd23b);
            ROM_LOAD("ng-5.bin", 0x50000, 0x8000, 0xa8ee3720);
            ROM_LOAD("nm-5.bin", 0x58000, 0x8000, 0xc100258e);

            ROM_REGION(0x20000, REGION_SOUND1);/* adpcm */
            ROM_LOAD("n5-5.bin", 0x00000, 0x8000, 0x7ee43a3c);
            ROM_LOAD("n4-5.bin", 0x10000, 0x8000, 0x6557564c);
            ROM_LOAD("n3-5.bin", 0x18000, 0x8000, 0x78fd6190);
            ROM_END();
        }
    };

    static RomLoadPtr rom_kuniokun = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1);/* 64k for code + bank switched ROM */
            ROM_LOAD("nb-01.bin", 0x08000, 0x8000, 0x93fcfdf5);// original
            ROM_LOAD("ta18-11.bin", 0x04000, 0x4000, 0xf240f5cd);
            ROM_CONTINUE(0x10000, 0x4000);

            ROM_REGION(0x10000, REGION_CPU2);/* audio CPU (M6809) */
            ROM_LOAD("n0-5.bin", 0x8000, 0x8000, 0x3587de3b);

            ROM_REGION(0x10000, REGION_CPU3);/* mcu (missing) */
            ROM_LOAD("mcu", 0x8000, 0x8000, 0x00000000);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ta18-25.bin", 0x0000, 0x8000, 0x9bd2bea3);/* characters */

            ROM_REGION(0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ta18-01.bin", 0x00000, 0x8000, 0xdaf15024);/* tiles */
            ROM_LOAD("ta18-06.bin", 0x08000, 0x8000, 0x1f59a248);
            ROM_LOAD("n7-5.bin", 0x10000, 0x8000, 0x7ca5a532);
            ROM_LOAD("ta18-02.bin", 0x18000, 0x8000, 0x994c0021);
            ROM_LOAD("ta18-04.bin", 0x20000, 0x8000, 0x55b9e8aa);
            ROM_LOAD("ta18-03.bin", 0x28000, 0x8000, 0x0475c99a);

            ROM_REGION(0x60000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ta18-20.bin", 0x00000, 0x8000, 0xc7d54139);/* sprites */
            ROM_LOAD("ta18-24.bin", 0x08000, 0x8000, 0x84677d45);
            ROM_LOAD("ta18-18.bin", 0x10000, 0x8000, 0x1c770853);
            ROM_LOAD("ta18-14.bin", 0x18000, 0x8000, 0xaf656017);
            ROM_LOAD("ta18-23.bin", 0x20000, 0x8000, 0x3fd19cf7);
            ROM_LOAD("ta18-17.bin", 0x28000, 0x8000, 0x74c64c6e);
            ROM_LOAD("ta18-19.bin", 0x30000, 0x8000, 0xc8795fd7);
            ROM_LOAD("ta18-22.bin", 0x38000, 0x8000, 0xdf3a2ff5);
            ROM_LOAD("ta18-16.bin", 0x40000, 0x8000, 0x7244bad0);
            ROM_LOAD("ta18-13.bin", 0x48000, 0x8000, 0xb6b14d46);
            ROM_LOAD("ta18-21.bin", 0x50000, 0x8000, 0xc95e009b);
            ROM_LOAD("ta18-15.bin", 0x58000, 0x8000, 0xa5d61d01);

            ROM_REGION(0x20000, REGION_SOUND1);/* adpcm */
            ROM_LOAD("ta18-07.bin", 0x00000, 0x8000, 0x02e3f3ed);
            ROM_LOAD("ta18-08.bin", 0x10000, 0x8000, 0xc9312613);
            ROM_LOAD("ta18-09.bin", 0x18000, 0x8000, 0x07ed4705);
            ROM_END();
        }
    };

    static RomLoadPtr rom_kuniokub = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1);/* 64k for code + bank switched ROM */
            ROM_LOAD("ta18-10.bin", 0x08000, 0x8000, 0xa90cf44a);// bootleg
            ROM_LOAD("ta18-11.bin", 0x04000, 0x4000, 0xf240f5cd);
            ROM_CONTINUE(0x10000, 0x4000);

            ROM_REGION(0x10000, REGION_CPU2);/* audio CPU (M6809) */
            ROM_LOAD("n0-5.bin", 0x8000, 0x8000, 0x3587de3b);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ta18-25.bin", 0x0000, 0x8000, 0x9bd2bea3);/* characters */

            ROM_REGION(0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ta18-01.bin", 0x00000, 0x8000, 0xdaf15024);/* tiles */
            ROM_LOAD("ta18-06.bin", 0x08000, 0x8000, 0x1f59a248);
            ROM_LOAD("n7-5.bin", 0x10000, 0x8000, 0x7ca5a532);
            ROM_LOAD("ta18-02.bin", 0x18000, 0x8000, 0x994c0021);
            ROM_LOAD("ta18-04.bin", 0x20000, 0x8000, 0x55b9e8aa);
            ROM_LOAD("ta18-03.bin", 0x28000, 0x8000, 0x0475c99a);

            ROM_REGION(0x60000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ta18-20.bin", 0x00000, 0x8000, 0xc7d54139);/* sprites */
            ROM_LOAD("ta18-24.bin", 0x08000, 0x8000, 0x84677d45);
            ROM_LOAD("ta18-18.bin", 0x10000, 0x8000, 0x1c770853);
            ROM_LOAD("ta18-14.bin", 0x18000, 0x8000, 0xaf656017);
            ROM_LOAD("ta18-23.bin", 0x20000, 0x8000, 0x3fd19cf7);
            ROM_LOAD("ta18-17.bin", 0x28000, 0x8000, 0x74c64c6e);
            ROM_LOAD("ta18-19.bin", 0x30000, 0x8000, 0xc8795fd7);
            ROM_LOAD("ta18-22.bin", 0x38000, 0x8000, 0xdf3a2ff5);
            ROM_LOAD("ta18-16.bin", 0x40000, 0x8000, 0x7244bad0);
            ROM_LOAD("ta18-13.bin", 0x48000, 0x8000, 0xb6b14d46);
            ROM_LOAD("ta18-21.bin", 0x50000, 0x8000, 0xc95e009b);
            ROM_LOAD("ta18-15.bin", 0x58000, 0x8000, 0xa5d61d01);

            ROM_REGION(0x20000, REGION_SOUND1);/* adpcm */
            ROM_LOAD("ta18-07.bin", 0x00000, 0x8000, 0x02e3f3ed);
            ROM_LOAD("ta18-08.bin", 0x10000, 0x8000, 0xc9312613);
            ROM_LOAD("ta18-09.bin", 0x18000, 0x8000, 0x07ed4705);
            ROM_END();
        }
    };

    public static GameDriver driver_renegade = new GameDriver("1986", "renegade", "renegade.java", rom_renegade, null, machine_driver_renegade, input_ports_renegade, init_renegade, ROT0, "Technos (Taito America license)", "Renegade (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_kuniokun = new GameDriver("1986", "kuniokun", "renegade.java", rom_kuniokun, driver_renegade, machine_driver_renegade, input_ports_renegade, init_kuniokun, ROT0, "Technos", "Nekketsu Kouha Kunio-kun (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_kuniokub = new GameDriver("1986", "kuniokub", "renegade.java", rom_kuniokub, driver_renegade, machine_driver_renegade, input_ports_renegade, null, ROT0, "bootleg", "Nekketsu Kouha Kunio-kun (Japan bootleg)", GAME_NO_COCKTAIL);
}
