package com.example.decodetest.player;

/**
 * Player's states corresponding to the states in libdecode.c
 * Created by leip on 2016/4/13.
 */
public class PlayerState {

        public static final int STATE_OFF    = 0;
        public static final int STATE_READY  = 1;
        public static final int STATE_PLAY   = 2;
        public static final int STATE_PAUSE  = 3;
        public static final int STATE_SHOOT  = 4;
        public static final int STATE_RECORD = 5;
        public static final int STATE_END_RECORD = 6;
        public static final int STATE_CLOSE  = 7;
        public static final int STATE_CLOSED = 8;

        private int state;
        public PlayerState(){
            state = STATE_OFF;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            if(state >= STATE_OFF && state <= STATE_CLOSED)
                this.state = state;
        }
}
