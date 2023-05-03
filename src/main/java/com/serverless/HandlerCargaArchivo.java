package com.serverless;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.json.JSONObject;

import java.io.*;
import java.util.Base64;

public class HandlerCargaArchivo implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

//    Configuración de conexión a Bucket S3
    private AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAWOZO3NH3CIGG5CZN", "Hg6G9uhBJchUrZFz6/slV8T7GyOLDBsxL7H4T/EJ")))
            .build();
    private String bucketName = "gluster-s3";

    //    Se sobre escribe handleRequest()
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event,
                                                      Context context) {


        //Recibo del event body el parametro de base64 y el nombre que llevará el .txt
        JSONObject jsonRequest = new JSONObject(event.getBody());
        byte[] file = Base64.getDecoder().decode(jsonRequest.getString("base64"));
        String name = jsonRequest.getString("nameFile");

        //Se crea objeto metadata necesario para guardar objeto en s3 desde un ByteArrayInputStream
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setContentLength(file.length);

        try {
            //Esto es para guardar el objeto en el bucket S3
            s3Client.putObject(new PutObjectRequest(bucketName, name, new ByteArrayInputStream(file), metadata));
        }catch (Exception e){
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(500);
            response.setBody("Error guardando: "+e.getMessage());
        }
        //se regresa una respuesta correcta
        return new APIGatewayProxyResponseEvent().withBody(name+", "+"Se ha guardado con éxito.");
    }
}
