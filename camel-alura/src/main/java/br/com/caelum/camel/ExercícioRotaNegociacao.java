package br.com.caelum.camel;

import java.text.SimpleDateFormat;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.thoughtworks.xstream.XStream;

public class ExercícioRotaNegociacao {
	
	private static MysqlConnectionPoolDataSource criaDataSource() {
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setDatabaseName("camel");
		dataSource.setServerName("localhost");
		dataSource.setPort(3306);
		dataSource.setUser("root");
		dataSource.setPassword("");
		return dataSource;
	}

	public static void main(String[] args) throws Exception {
		SimpleRegistry registry = new SimpleRegistry();
		registry.put("mysql", criaDataSource());
		CamelContext context = new DefaultCamelContext(registry); //construtor recebe um registro
		
		final XStream xStream = new XStream();
		
		xStream.alias("negociacao", Negociacao.class); //transforma o xml em objeto
		
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s")
						.to("http4://argentumws.caelum.com.br/negociacoes")
						.convertBodyTo(String.class)
						.log("${body}")
						.unmarshal(new XStreamDataFormat(xStream)) //unmarshal do xml para objeto
						.split(body())
						.process(new Processor() {
							
							@Override
							public void process(Exchange exchange) throws Exception {
								Negociacao negociacao = exchange.getIn().
								getBody(Negociacao.class);
								
								exchange.setProperty("preco", negociacao.getPreco());
								exchange.setProperty("quantidade", negociacao.getQuantidade());
								String data = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
								exchange.setProperty("data", data);
							}
						})
						.setBody(simple("insert into negociacao(preco, quantidade, data) "
										+ "values (${property.preco}, ${property.quantidade}, '${property.data}')"))
					    .log("${body}") //logando o comando esql
					    .delay(1000) //esperando 1s para deixar a execução mais fácil de entender
					    .to("jdbc:mysql"); //usando o componente jdbc que envia o SQL para mysql
			}
		});
		context.start();
		Thread.sleep(20000);
		context.stop();
	}

}
