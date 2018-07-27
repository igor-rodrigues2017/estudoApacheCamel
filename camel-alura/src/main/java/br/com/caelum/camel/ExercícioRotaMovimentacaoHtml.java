package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class ExercícioRotaMovimentacaoHtml {
	

	public static void main(String[] args) throws Exception {
		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("file:movimentacao?delay=5s&noop=true:")
						.to("xslt:movimentacao-para-html.xslt")
						.setHeader(Exchange.FILE_NAME, constant("movimentacaoes.html"))
						.log("${body}")
						.to("file:saida");
			}
		});
		context.start();
		Thread.sleep(20000);
		context.stop();
	}

}
