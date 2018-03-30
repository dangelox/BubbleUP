package com.example.bubbleup;

public class DeepEmoji {
    public static final int bit_32 = 32;

    public static final int ANALYZED_MASK = 0xE0000000;
    public static final int ANALYZED_SHIFT = bit_32 - 3;

    public static final int CONTENT_TYPE_MASK = 0x1F000000;
    public static final int CONTENT_TYPE_SHIFT = ANALYZED_SHIFT - 5;

    public static final int EMOJI_NUM_MASK = 0xE00000;
    public static final int EMOJI_NUM_SHIFT = CONTENT_TYPE_SHIFT - 3;

    public static final int EMOJI_1_MASK = 0x1F8000;
    public static final int EMOJI_1_SHIFT = EMOJI_NUM_SHIFT - 6;

    public static final int EMOJI_2_MASK = 0x7E00;
    public static final int EMOJI_2_SHIFT = EMOJI_1_SHIFT - 6;

    public static final int EMOJI_3_MASK = 0x1F8;
    public static final int EMOJI_3_SHIFT = EMOJI_2_SHIFT - 6;

    public static final int SENTIMENT_MASK = 0x7;
    public static final int SENTIMENT_SHIFT = EMOJI_3_SHIFT - 3;

    public static String [] emojiArray =  {
        "\uD83D\uDE02",
                "\uD83D\uDE12",
                "\uD83D\uDE29",
                "\uD83D\uDE2D",
                "\uD83D\uDE0D",
                "\uD83D\uDE14",
                "\uD83D\uDC4C",
                "\uD83D\uDE0A",
                "❤",
                "\uD83D\uDE0F",
                "\uD83D\uDE01",
                "\uD83C\uDFB6",
                "\uD83D\uDE33",
                "\uD83D\uDCAF",
                "\uD83D\uDE34",
                "\uD83D\uDE0C",
                "☺",
                "\uD83D\uDE4C",
                "\uD83D\uDC95",
                "\uD83D\uDE11",
                "\uD83D\uDE05",
                "\uD83D\uDE4F",
                "\uD83D\uDE15",
                "\uD83D\uDE18",
                "♥",
                "\uD83D\uDE10",
                "\uD83D\uDC81",
                "\uD83D\uDE1E",
                "\uD83D\uDE48",
                "\uD83D\uDE2B",
                "✌",
                "\uD83D\uDE0E",
                "\uD83D\uDE21",
                "\uD83D\uDC4D",
                "\uD83D\uDE22",
                "\uD83D\uDE2A",
                "\uD83D\uDE0B",
                "\uD83D\uDE24",
                "✋",
                "\uD83D\uDE37",
                "\uD83D\uDC4F",
                "\uD83D\uDC40",
                "\uD83D\uDD2B",
                "\uD83D\uDE23",
                "\uD83D\uDE08",
                "\uD83D\uDE13",
                "\uD83D\uDC94",
                "\u2661",
                "\uD83C\uDFA7",
                "\uD83D\uDE4A",
                "\uD83D\uDE09",
                "\uD83D\uDC80",
                "\uD83D\uDE16",
                "\uD83D\uDE04",
                "\uD83D\uDE1C",
                "\uD83D\uDE20",
                "\uD83D\uDE45",
                "\uD83D\uDCAA",
                "\uD83D\uDC4A",
                "\uD83D\uDC9C",
                "\uD83D\uDC96",
                "\uD83D\uDC99",
                "\uD83D\uDE2C",
                "✨"
    };
    /*
    Alternative
    emojiArray = new String [] {
            "\uf602",
            "\uf612",
            "\uf629",
            "\uf62d",
            "\uf60d",
            "\uf614",
            "\uf44c",
            "\uf60a",
            "\u2764",
            "\uf60f",
            "\uf601",
            "\uf3b6",
            "\uf633",
            "\uf4af",
            "\uf634",
            "\uf60c",
            "\u263a",
            "\uf64c",
            "\uf495",
            "\uf611",
            "\uf605",
            "\uf64f",
            "\uf615",
            "\uf618",
            "\u2665",
            "\uf610",
            "\uf481",
            "\uf61e",
            "\uf648",
            "\uf62b",
            "\u270c",
            "\uf60e",
            "\uf621",
            "\uf44d",
            "\uf622",
            "\uf62a",
            "\uf60b",
            "\uf624",
            "\u270b",
            "\uf637",
            "\uf44f",
            "\uf440",
            "\uf52b",
            "\uf623",
            "\uf608",
            "\uf613",
            "\uf494",
            "\u2661",
            "\uf3a7",
            "\uf64a",
            "\uf609",
            "\uf480",
            "\uf616",
            "\uf604",
            "\uf61c",
            "\uf620",
            "\uf645",
            "\uf4aa",
            "\uf44a",
            "\uf49c",
            "\uf496",
            "\uf499",
            "\uf62c",
            "\u2728"
        };
     */
}
