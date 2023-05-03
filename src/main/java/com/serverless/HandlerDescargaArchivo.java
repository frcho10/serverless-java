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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.nio.ByteBuffer;
import java.util.Base64;

public class HandlerDescargaArchivo implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    //    Configuración de conexión a Bucket S3
    private AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("", "")))
            .build();
    private String bucketName = "gluster-s3";

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event,
                                                      Context context) {

        //Se obtiene el parametro de la uri
        String nameFile = event.getQueryStringParameters().get("nameFile");
        //la siguiente línea lo que hace es buscar el nombre del objeto en el bucket de s3
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, nameFile));
        ByteBuffer byteBuffer = null;
        String base64EncodedString  = null;
        try {
            //se lee el objeto en arreglo de bytes y se convierte en buffer para leerlo en base64
            byteBuffer = ByteBuffer.wrap(s3Object.getObjectContent().readAllBytes());
            base64EncodedString = "data:text/plain;base64," + new String(Base64.getEncoder().encode(byteBuffer.array()), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(base64EncodedString);

        return response;
    }
}