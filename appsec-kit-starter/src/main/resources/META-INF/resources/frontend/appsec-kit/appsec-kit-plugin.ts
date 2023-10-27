/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
import { LitElement, html, css } from "lit";
import { customElement, property } from "lit/decorators.js";
import { DevToolsInterface, DevToolsPlugin, MessageHandler, MessageType, ServerMessage, VaadinDevTools } from "@vaadin/flow-frontend/vaadin-dev-tools/vaadin-dev-tools";

const devTools: VaadinDevTools = (window as any).Vaadin.devTools;

@customElement("appsec-kit-plugin")
export class AppSecKitPlugin extends LitElement implements MessageHandler {

    static APPSEC_KIT_URI = "/vaadin-appsec-kit";

    static styles = css`
        .container {
            display: flex;
            padding: 0.75rem;
            justify-content: space-between;
        }
    `;

    @property()
    message: string = "No data available yet.";

    render() {
        return html`
            <div class="container">
                <span>${this.message}</span>
                <button class="tab" @click="${this.openAppSecKit}">Open AppSec Kit</button>
            </div>
        `;
    }

    openAppSecKit() {
        window.open(AppSecKitPlugin.APPSEC_KIT_URI, "_blank");
    }

    handleMessage(message: ServerMessage): boolean {
        console.log("appsec-kit-plugin command received: " + message.command);
        if (message.command === "appsec-kit-init") {
            devTools.showNotification("information" as MessageType, "AppSec Kit is running",
                    "AppSec Kit is configured and scanning app dependencies for known vulnerabilities.",
                    AppSecKitPlugin.APPSEC_KIT_URI, "appsec-kit-running");
            return true;
        } else if (message.command === "appsec-kit-scan") {
            if (message.data.vulnerabilityCount > 0) {
                devTools.showNotification("error" as MessageType, "Potential vulnerabilities found");
                this.message = message.data.vulnerabilityCount + " potential vulnerabilities found.";
            } else {
                devTools.showNotification("information" as MessageType, "No vulnerabilities found");
                this.message = "No vulnerabilities found."
            }
            return true;
        } else {
            return false; // not a plugin command
        }
    }
}

const plugin: DevToolsPlugin = {
    init: (devToolsInterface: DevToolsInterface): void => {
        devToolsInterface.addTab("AppSec Kit", "appsec-kit-plugin");
    }
};
(window as any).Vaadin.devToolsPlugins.push(plugin);
