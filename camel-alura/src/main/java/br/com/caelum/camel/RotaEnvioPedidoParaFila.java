package br.com.caelum.camel;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaEnvioPedidoParaFila {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		//adiconar o activemq
		context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616"));
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				
				from("file:pedidos")
					.to("activemq:queue:pedidos");
			}

		});

		context.start();
		Thread.sleep(20000);
		context.stop();
	}
}
