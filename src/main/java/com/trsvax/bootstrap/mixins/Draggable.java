package com.trsvax.bootstrap.mixins;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Visitor;
import org.apache.tapestry5.internal.services.ArrayEventContext;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.got5.tapestry5.jquery.ImportJQueryUI;
import org.slf4j.Logger;

@ImportJQueryUI(value = {"jquery.ui.widget", "jquery.ui.mouse", "jquery.ui.draggable"})
@Import(library = { "classpath:com/trsvax/bootstrap/assets/mixins/draggable/draggable.js" })

@MixinAfter
public class Draggable {

	@Inject
    TypeCoercer typeCoercer;
	@Inject
    ContextPathEncoder contextPathEncoder;
	@Inject
	JavaScriptSupport javaScriptSupport;
    @Parameter(defaultPrefix="literal")
    JSONObject params;
	@Inject
	ComponentResources resources;
	@Parameter
	private String elementName;
	@Parameter
	private String draggableName;
    @Parameter
    private Object[] context;
	private Element element;
	private JSONObject spec;
	@Inject
	private Logger logger;

	@SetupRender
	void beginRender() {
        if (params == null) {
            params = new JSONObject();
        }
        putConditionally("appendTo", "body");
        putConditionally("helper", "original");
        spec = new JSONObject().put("params", params);
    }

    private void putConditionally(String key, Object value) {
        if (!params.has(key)) {
            params.put(key, value);
        }
    }

    @AfterRender
	public void afterRender(MarkupWriter writer) {
		String id;
		if ( elementName == null ) {
			elementName = resources.getElementName();
			if ( elementName == null ) {
				elementName = "ul";
			}
		}
		if ( draggableName == null ) {
			draggableName = "div";
			if ( elementName.equals("ul")) {
					draggableName = "li";
			}
		}

		Object compoment =  resources.getContainer();
		if ( ClientElement.class.isAssignableFrom(compoment.getClass()) ) {
			id = ((ClientElement)compoment).getClientId();
		} else {
			id = javaScriptSupport.allocateClientId(resources);
		}
		if ( Grid.class.isAssignableFrom(compoment.getClass()) ) {
			elementName = "tbody";
			draggableName = "tr";
		}

		element = writer.getElement();

        element.visit( new Visitor() {

            public void visit(Element e) {
                if ( e.getName().equals(elementName)) {
                    element = e;
                }
                if ( e.getName().equals("tr"))  {
                    String c = e.getAttribute("class");
                    if ( c != null ) {
                        e.forceAttributes("id",c.split(" ")[0]);
                    }
                }

            }
        });
        String currentID = element.getAttribute("id");
        if ( currentID != null ) {
            id = currentID;
        } else {
            element.forceAttributes("id",id);
        }
        //element.addClassName("sortable");
        //logger.info("spec {}",spec);
		if ( ! spec.has("selector")) {
			spec.put("selector",String.format("#%s %s",id,draggableName));
		}
        if (!spec.has("context")) {
            ArrayEventContext aec = new ArrayEventContext(typeCoercer, defaulted(context));
            spec.put("context", String.format("/%s", contextPathEncoder.encodeIntoPath(aec)));
        }
        javaScriptSupport.addInitializerCall("jqDraggable", spec);
    }

    private Object[] defaulted(Object[] context) {
        return context == null ? new String[0] : context;
    }

    public JSONObject getSpec() {
		return spec;
	}
}
