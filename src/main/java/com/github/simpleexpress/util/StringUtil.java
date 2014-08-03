package com.github.simpleexpress.util;

import java.util.List;



public class StringUtil
{
    public static String join(final String delimiter, final List list)
    {
        StringBuilder sb = new StringBuilder(list.get(0).toString());
        int size = list.size();

        if (size > 1)
        {
            for (int i = 1; i < size; i++)
            {
                sb.append(delimiter);
                sb.append(list.get(i).toString());
            }
        }
        return sb.toString();
    }

    public static String toString(Object obj)
    {
        if (obj == null)
        {
            return "null";
        }
        return obj.toString();
    }

    public static String[] indexedSplit(String input, String delimiter, final int[] wanted)
    {
        String[] group = input.split(delimiter);
        String[] output = new String[wanted.length];
        for (int i = 0; i < wanted.length; i++)
        {
            int index = wanted[i];
            if (group.length <= index)
            {
                output[i] = "";
            }
            else
            {
                output[i] = group[index];
            }
        }

        return output;
    }
}
