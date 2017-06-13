package edu.cmu.inmind.multiuser.sara.component.beat;

import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.usc.ict.vhmsg.MessageEvent;
import edu.usc.ict.vhmsg.MessageListener;
import edu.usc.ict.vhmsg.VHMsg;

/**
 * Created by yoichimatsuyama on 4/23/17.
 */
public class BEATSimple implements MessageListener {
    VHMsg vhmsg;
    BeatCallback callback;

    public BEATSimple(){
        System.setProperty("VHMSG_SERVER", Config.VHMSG_SERVER_URL);
        vhmsg = new VHMsg();
        vhmsg.openConnection();
        vhmsg.enableImmediateMethod();
        vhmsg.addMessageListener(this);
        vhmsg.subscribeMessage("vrSpeak");
    }

    public void sendMessage(SROutput srOutput, String sentence){
        String vrExpress = createVrExpress(srOutput, sentence);
        vhmsg.sendMessage("vrExpress " + vrExpress);
    }

    public String createVrExpress(SROutput srOutput, String sentence){
        String vrExpress = "Brad user 0 <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?> \n" +
                "<act> <participant id=\"Brad\" role=\"actor\" /><fml> \n" +
                "<turn continuation=\"false\" />\n" +
                "<affect type=\"neutral\" target=\"addressee\">\n" +
                "</affect> <culture type=\"neutral\"> </culture>\n" +
                "<personality type=\"neutral\"> </personality>\n" +
                "<sentence phase=\"greetings\" intention=\"" + srOutput.getAction() + "\" " +
                "strategy=\"" + srOutput.getStrategy() + "\" " +
                "rapport=\"" + srOutput.getRapport() + "\" " +
                "text=\"" + sentence  +
                ".\" />\n" +
                "</fml>\n" +
                "<bml>\n" +
                "<speech>\n" +
                sentence + "</speech>\n" +
                "</bml>\n" +
                "<ssml>\n" +
                "<speech>" + sentence + "</speech>\n" +
                "</ssml>\n" +
                "</act>";

        return vrExpress;
    }

    @Override
    public void messageAction(MessageEvent messageEvent) {
        String m = messageEvent.toString();
        String[] split =  m.split(" ");
        if(split[0].equals("vrSpeak")){
            String bson = m.substring(m.indexOf("{"));
            callback.receiveMessage(bson);
        }
    }

    public void addMessageListener(BeatCallback callback){
        this.callback = callback;
    }

}
