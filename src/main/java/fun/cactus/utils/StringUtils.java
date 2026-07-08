package fun.cactus.utils;

public final class StringUtils {
    private StringUtils(){}

    public static int getLastChar(String str, char c, int n) {
        int pos;
        for (pos = str.lastIndexOf(c, str.length()); n-- > 1 && pos != -1; pos = str.lastIndexOf(c, pos - 1)) {
        }

        return pos;
    }
}
