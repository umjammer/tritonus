package org.tritonus.lowlevel.gsm;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Contains the "exploded" parameters of a GSM frame.
 *
 * <p>
 * These are the parameters after bit-decoding or before bit-encoding.
 *
 * <table border="1">
 * <tr>
 * <th>Parameter name</th>
 * <th>Variable name</th>
 * <th>number of parameters</th>
 * </tr>
 * <tr>
 * <td>LAR</td>
 * <td>larC</td>
 * <td>8</td>
 * </tr>
 * <tr>
 * <td>N</td>
 * <td>nc</td>
 * <td>4</td>
 * </tr>
 * <tr>
 * <td>b</td>
 * <td>bc</td>
 * <td>4</td>
 * </tr>
 * <tr>
 * <td>M</td>
 * <td>mc</td>
 * <td>4</td>
 * </tr>
 * <tr>
 * <td>Xmax</td>
 * <td>xMaxC</td>
 * <td>4</td>
 * </tr>
 * <tr>
 * <td>x</td>
 * <td>xmc</td>
 * <td>52 (13 * 4)</td>
 * </tr>
 * </table>
 *
 * @author Matthias Pfisterer
 */
public class GsmFrameParameters {

    public int[] larC = new int[8];
    public int[] nc = new int[4];
    public int[] mc = new int[4];
    public int[] bc = new int[4];
    public int[] xMaxC = new int[4];
    public int[] xmc = new int[13 * 4];

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("GSM frame:");
        for (int i = 0; i < 8; i++) {
            pw.println("larC[" + i + "]" + larC[i]);
        }
        for (int i = 0; i < 4; i++) {
            pw.println("nc[" + i + "]" + nc[i]);
            pw.println("bc[" + i + "]" + bc[i]);
            pw.println("mc[" + i + "]" + mc[i]);
            pw.println("xMaxC[" + i + "]" + xMaxC[i]);
            for (int j = 0; j < 13; j++) {
                pw.println("xmc[" + i * 13 + j + "]" + xmc[i * 13 + j]);
            }
        }
        return sw.toString();
    }
}
