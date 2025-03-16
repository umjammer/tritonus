/*
 *  Copyright (c) 2000 - 2001 by Matthias Pfisterer
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.tritonus.lowlevel.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * TODO
 */
public class AlsaPcmHWParams {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaPcmNative");

    /**
     * Holds the pointer to snd_pcm_hw_params_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    public AlsaPcmHWParams() {
        logger.log(Level.TRACE, "AlsaPcmHWParams.<init>(): begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of hw_params failed");
        }

        logger.log(Level.TRACE, "AlsaPcmHWParams.<init>(): end");
    }

    private native int malloc();

    public native void free();

    /**
     * Calls snd_pcm_hw_params_get_rate_numden().
     * <p>
     * values[0]: numerator
     * values[1]: denominator
     */
    public native int getRate(long[] values);

    public double getRate() {
        long[] values = new long[2];

        int ret = getRate(values);
        double dRate = -1;
        if (ret >= 0) {
            dRate = (double) values[0] / (double) values[1];
        }
        return dRate;
    }

    public native int getSBits();

    public native int getFifoSize();

    public native int getAccess();

    public native int getFormat();

    public native void getFormatMask(AlsaPcmHWParamsFormatMask mask);

    public native int getSubformat();

    public native int getChannels();

    public native int getChannelsMin();

    public native int getChannelsMax();

    /**
     * Gets approximate rate.
     * Calls snd_pcm_hw_params_get_rate().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getRate(int[] values);

    /**
     * Gets approximate minimum rate.
     * Calls snd_pcm_hw_params_get_rate_min().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getRateMin(int[] values);

    /**
     * Gets approximate maximum rate.
     * Calls snd_pcm_hw_params_get_rate_max().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getRateMax(int[] values);

    /**
     * Gets approximate period time.
     * Calls snd_pcm_hw_params_get_period_time().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodTime(int[] values);

    /**
     * Gets approximate minimum period time.
     * Calls snd_pcm_hw_params_get_period_time_min().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodTimeMin(int[] values);

    /**
     * Gets approximate maximum period time.
     * Calls snd_pcm_hw_params_get_period_time_max().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodTimeMax(int[] values);

    /**
     * Gets approximate period size.
     * Calls snd_pcm_hw_params_get_period_size().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodSize(int[] values);

    /**
     * Gets approximate minimum period size.
     * Calls snd_pcm_hw_params_get_period_size_min().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodSizeMin(int[] values);

    /**
     * Gets approximate maximum period size.
     * Calls snd_pcm_hw_params_get_period_size_max().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodSizeMax(int[] values);

    /**
     * Gets approximate periods.
     * Calls snd_pcm_hw_params_get_periods().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriods(int[] values);

    /**
     * Gets approximate minimum periods.
     * Calls snd_pcm_hw_params_get_periods_min().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodsMin(int[] values);

    /**
     * Gets approximate maximum periods.
     * Calls snd_pcm_hw_params_get_periods_max().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getPeriodsMax(int[] values);

    /**
     * Gets approximate buffer time.
     * Calls snd_pcm_hw_params_get_buffer_time().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getBufferTime(int[] values);

    /**
     * Gets approximate minimum buffer time.
     * Calls snd_pcm_hw_params_get_buffer_time_min().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getBufferTimeMin(int[] values);

    /**
     * Gets approximate maximum buffer time.
     * Calls snd_pcm_hw_params_get_buffer_time_max().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getBufferTimeMax(int[] values);

    /**
     * Gets approximate buffer size.
     * Calls snd_pcm_hw_params_get_buffer_size().
     */
    public native int getBufferSize();

    /**
     * Gets approximate minimum buffer size.
     * Calls snd_pcm_hw_params_get_buffer_size_min().
     */
    public native int getBufferSizeMin();

    /**
     * Gets approximate maximum buffer size.
     * Calls snd_pcm_hw_params_get_buffer_size_max().
     */
    public native int getBufferSizeMax();

    /**
     * Gets approximate tick time.
     * Calls snd_pcm_hw_params_get_tick_time().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getTickTime(int[] values);

    /**
     * Gets approximate minimum tick time.
     * Calls snd_pcm_hw_params_get_tick_time_min().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getTickTimeMin(int[] values);

    /**
     * Gets approximate maximum tick time.
     * Calls snd_pcm_hw_params_get_tick_time_max().
     * values[0]: -1, 0 or +1, depending on the direction the exact rate differs from the returned value.
     */
    public native int getTickTimeMax(int[] values);
}
