package org.com;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.internal.Request;
import com.chargebee.models.Customer;
import com.chargebee.models.PaymentSource;
import com.chargebee.models.enums.Role;
import com.chargebee.models.enums.Type;
import javafx.util.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Markbackup {

    static CSVPrinter outputPrinter;


    public static void main(String[] args) throws Exception {

        Environment.configure("", "");
        String inputFile = "";
        String outputFile = "";
        int batchSize = 10;
        int sleepTime = 2;

          try {
              File reportFile = new File(outputFile + "status.csv");
              BufferedWriter outWriter = Files.newBufferedWriter(Paths.get(reportFile.getPath()));
              outputPrinter = new CSVPrinter(outWriter, CSVFormat.EXCEL);
              outputPrinter.printRecord("customer_id", "prim_pm", "back_pm","unknown_pm","new_ip");

              CSVParser parser = new CSVParser(new FileReader(inputFile), CSVFormat.EXCEL.withHeader());
              List<CSVRecord> records = parser.getRecords();

              int count = 0;
              for (CSVRecord rec : records) {
                  execute(rec.get("customer_id"),rec.get("cc_external_id"),rec.get("type"),rec.get("ip_address"));
                  if (count % batchSize == 0) {
                      Thread.sleep(sleepTime);
                  }
                  count++;
              }
          }catch (Exception e){
              e.printStackTrace();
          }
          finally {
              outputPrinter.flush();
              outputPrinter.close();
          }

    }
    public static void execute(String customer_id,String pm_id,String role,String ip_address) throws Exception{
        Role paymentRole =null;
        //delete local
        PaymentSource localPaymentSource = deletelocal(pm_id);
        //re-import
        PaymentSource importPayment = createUsingPermanentToken(customer_id,localPaymentSource,ip_address);



        if(role.equals("PRIMARY")){
            paymentRole = Role.PRIMARY;
        }
        else if(role.equals("BACKUP")){
            paymentRole = Role.BACKUP;
        }
        if(paymentRole!=null){
            //assign role
            Result finalOut = assignRole(importPayment,paymentRole);
            Customer customer = finalOut.customer();
            PaymentSource paymentSource = finalOut.paymentSource();
            outputPrinter.printRecord(customer.id(),customer.primaryPaymentSourceId(),customer.backupPaymentSourceId(),"",paymentSource.ipAddress());
        }
        else {
            Customer customer= Customer.retrieve(importPayment.customerId()).request().customer();
            outputPrinter.printRecord(customer.id(),customer.primaryPaymentSourceId(),customer.backupPaymentSourceId(),importPayment.id(),importPayment.ipAddress());
        }
    }

    public static PaymentSource deletelocal(String pm_src_id) throws Exception{
        Request req = PaymentSource.deleteLocal(pm_src_id);
        req.header("chargebee-event-email", "all-disabled");
        PaymentSource localPaymentSource = req.request().paymentSource();
        return localPaymentSource;
    }
    public static PaymentSource createUsingPermanentToken(String cus_id,PaymentSource localPaymentSource,String ipAddr) throws Exception{

        Result result = PaymentSource.createUsingPermanentToken()
                .customerId(cus_id)
                .referenceId(localPaymentSource.referenceId())
                .gatewayAccountId(localPaymentSource.gatewayAccountId())
                .type(localPaymentSource.type()).header("chargebee-request-origin-ip", ipAddr)
                .header("chargebee-event-email", "all-disabled")
                .request();
        return result.paymentSource();
    }
    public static Result assignRole(PaymentSource pm_src,Role role) throws Exception{
        Result assignPayment = Customer.assignPaymentRole(pm_src.customerId())
                .paymentSourceId(pm_src.id())
                .role(role)
                .header("chargebee-event-email", "all-disabled")
                .request();
        return assignPayment;

    }
}
