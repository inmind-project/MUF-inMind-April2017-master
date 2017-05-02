package edu.cmu.inmind.multiuser.sara.component.nlg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserFrame;

/**
 * Created by yoichimatsuyama on 4/11/17.
 */
public class Main {
    public static void main(String[] args){
        String json_dm = "{\n" +
                "  \"action\": \"explicit_confirm\",\n" +
                "  \"entities\": [\n" +
                "    {\n" +
                "      \"entity\": \"genres\",\n" +
                "      \"polarity\": 0.5,\n" +
                "      \"value\": \"drama\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"recommendation\": {\n" +
                "    \"rexplanations\": [\n" +
                "      {\n" +
                "        \"recommendation\": \"Toy Story (1995)\",\n" +
                "        \"explanations\": [\n" +
                "          \"stomhanks\"\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"userFrame\": {\n" +
                "    \"frame\": {\n" +
                "      \"actors\": {\n" +
                "        \"like\": [\n" +
                "          \"tom_cruise\",\n" +
                "          \"tom_hanks\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"harrison_ford\",\n" +
                "          \"carrie_fisher\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"genres\": {\n" +
                "        \"like\": [\n" +
                "          \"sci-fi\",\n" +
                "          \"drama\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"documentary\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"directors\": {\n" +
                "        \"like\": [\n" +
                "          \"steven_spielberg\",\n" +
                "          \"christopher_nolan\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"quentin_tarantino\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"movies\": {\n" +
                "        \"like\": [\n" +
                "          \"toy_story\",\n" +
                "          \"intersteller\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"et\"\n" +
                "        ],\n" +
                "        \"history\": [\n" +
                "          \"???\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"ask_stack\": [\n" +
                "      \"start\",\n" +
                "      \"genres\",\n" +
                "      \"directors\",\n" +
                "      \"actors\",\n" +
                "      \"recommend\"\n" +
                "    ],\n" +
                "    \"universals\": [\n" +
                "      \"help\",\n" +
                "      \"start_over\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        String json_sr = "{\n" +
                "  \"action\": \"explicit_confirm\",\n" +
                "  \"strategy\": \"SD\",\n" +
                "  \"rapport\": 4,\n" +
                "  \"entities\": [\n" +
                "    {\n" +
                "      \"entity\": \"genres\",\n" +
                "      \"polarity\": 0.5,\n" +
                "      \"value\": \"drama\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"recommendation\": {\n" +
                "    \"rexplanations\": [\n" +
                "      {\n" +
                "        \"recommendation\": \"Toy Story (1995)\",\n" +
                "        \"explanations\": [\n" +
                "          \"stomhanks\"\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"userFrame\": {\n" +
                "    \"frame\": {\n" +
                "      \"actors\": {\n" +
                "        \"like\": [\n" +
                "          \"tom_cruise\",\n" +
                "          \"tom_hanks\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"harrison_ford\",\n" +
                "          \"carrie_fisher\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"genres\": {\n" +
                "        \"like\": [\n" +
                "          \"sci-fi\",\n" +
                "          \"drama\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"documentary\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"directors\": {\n" +
                "        \"like\": [\n" +
                "          \"steven_spielberg\",\n" +
                "          \"christopher_nolan\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"quentin_tarantino\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"movies\": {\n" +
                "        \"like\": [\n" +
                "          \"toy_story\",\n" +
                "          \"intersteller\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"et\"\n" +
                "        ],\n" +
                "        \"history\": [\n" +
                "          \"???\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"ask_stack\": [\n" +
                "      \"start\",\n" +
                "      \"genres\",\n" +
                "      \"directors\",\n" +
                "      \"actors\",\n" +
                "      \"recommend\"\n" +
                "    ],\n" +
                "    \"universals\": [\n" +
                "      \"help\",\n" +
                "      \"start_over\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        String json_userFrame = "{\n" +
                "    \"frame\": {\n" +
                "        \"actors\": {\n" +
                "            \"like\": [\"tom_cruise\", \"tom_hanks\"],\n" +
                "            \"dislike\": [\"harrison_ford\", \"carrie_fisher\"]\n" +
                "        },\n" +
                "        \"genres\": {\n" +
                "            \"like\": [\"sci-fi\", \"drama\"],\n" +
                "            \"dislike\": [\"documentary\"]\n" +
                "        },\n" +
                "        \"directors\": {\n" +
                "            \"like\": [\"steven_spielberg\", \"christopher_nolan\"],\n" +
                "            \"dislike\": [\"quentin_tarantino\"]\n" +
                "        },\n" +
                "        \"movies\": {\n" +
                "            \"like\": [\"toy_story\", \"intersteller\"],\n" +
                "            \"dislike\": [\"et\"],\n" +
                "            \"history\": [\"???\"]\n" +
                "        }\n" +
                "    },\n" +
                "    \"ask_stack\": [\"start\", \"genres\", \"directors\", \"actors\", \"recommend\"],\n" +
                "    \"universals\": [\"help\", \"start_over\"]\n" +
                "}\n" +
                "\n";

        Gson gson = new GsonBuilder().create();

        DMOutput dmOutput = gson.fromJson(json_dm, DMOutput.class);
        SROutput srOutput = gson.fromJson(json_sr, SROutput.class);
        UserFrame userFrame = gson.fromJson(json_userFrame, UserFrame.class);

        System.out.println(gson.toJson(dmOutput));
        System.out.println(gson.toJson(srOutput));
        System.out.println(gson.toJson(userFrame));

        SentenceGeneratorTemplate gen = new SentenceGeneratorTemplate();
        System.out.println(gen.genenete(srOutput));
    }

}
