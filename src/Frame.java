/**
 * Created by Ein fugel on 01.05.2017.
 */
public class Frame {
    public final int FINAL;
    public final int RSV1;
    public final int RSV2;
    public final int RSV3;
    public final int OPCODE;
    public final byte[] MASK;
    public final int PAYLOADLEN;
    public final byte[] payload;

    public Frame(int FINAL, int RSV1, int RSV2, int RSV3, int OPCODE, byte[] MASK, int PAYLOADLEN, byte[] payload) {
        this.FINAL = FINAL;
        this.RSV1 = RSV1;
        this.RSV2 = RSV2;
        this.RSV3 = RSV3;
        this.OPCODE = OPCODE;
        this.MASK = MASK;
        this.PAYLOADLEN = PAYLOADLEN;
        this.payload = payload;
    }
}
