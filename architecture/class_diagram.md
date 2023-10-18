# Architecture and Design for Product

```mermaid

classDiagram
    class HttpService{
        + HttpService(url:String, resource: String)
        - getResource(url, resource)
        + getByteString() Source[ByteString, NotUsed]
    }


```