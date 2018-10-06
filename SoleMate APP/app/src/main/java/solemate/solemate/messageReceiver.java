package solemate.solemate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.Map;

public class messageReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        System.out.println("messageReceiver");

        if(bundle != null)
        {
//            String info = "SMS received from ";
            String info = "";
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus != null ? pdus.length : 0];
            byte[] data = null;

            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])(pdus != null? pdus[i]: 0));
//                info += msgs[i].getOriginatingAddress();
//                info += "\n*****MESSAGE*****\n";
//                info += msgs[i].getMessageBody();
//                info += "\n";

//                This is for Data Message Port
                try {
                    data = msgs[i].getUserData();
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println(e);
                }

                for(int index=0; index<data.length; ++index)
                {
                    info += Character.toString((char)data[index]);
                }
            }

            Toast.makeText(context, info, Toast.LENGTH_SHORT).show();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("SMS_RECEIVED_ACTION");
            broadcastIntent.putExtra("message", info);
            context.sendBroadcast(broadcastIntent);

        }
    }
}
