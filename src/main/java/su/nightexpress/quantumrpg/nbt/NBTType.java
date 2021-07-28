package su.nightexpress.quantumrpg.nbt;

import java.util.Arrays;

public enum NBTType {
    NBTTagEnd(0),
    NBTTagByte(1),
    NBTTagShort(2),
    NBTTagInt(3),
    NBTTagLong(4),
    NBTTagFloat(5),
    NBTTagDouble(6),
    NBTTagByteArray(7),
    NBTTagIntArray(11),
    NBTTagString(8),
    NBTTagList(9),
    NBTTagCompound(10);

    private final int id;

    NBTType(int i) {
        this.id = i;
    }

    public static NBTType valueOf(int id) {
        return Arrays.stream(values()).filter(t -> t.id == id).findFirst().orElse(null);
    }

    public int getId() {
        return this.id;
    }
}
