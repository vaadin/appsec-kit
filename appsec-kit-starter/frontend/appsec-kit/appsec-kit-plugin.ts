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
import type { CopilotInterface, CopilotPlugin, PanelConfiguration, MessageHandler, ServerMessage } from "../copilot/copilot-plugin-support.js";

@customElement("appsec-kit-plugin")
export class AppSecKitPlugin extends LitElement implements MessageHandler {

    static styles = css`
        .container {
            align-items: center;
            display: flex;
            padding: 0.75rem;
            justify-content: space-between;
        }
        .open-appsec-kit-button {
            align-items: center;
            background: var(--gray-100);
            border: 1px solid transparent;
            border-radius: var(--radius-1);
            color: var(--color-high-contrast);
            display: flex;
            font: var(--font-xsmall-medium);
            flex-shrink: 0;
            gap: var(--space-75);
            height: 1.75rem;
            justify-content: center;
            padding: 0 var(--space-100);

            &:hover {
                background: var(--gray-200);
            }
        }
    `;

    @property()
    appSecRoute: string = "/vaadin-appsec-kit";

    @property()
    message: string = "No data available yet.";

    render() {
        return html`
            <div class="container">
                <span>${this.message}</span>
                <button id="open-appsec-kit" class="open-appsec-kit-button" @click="${this.openAppSecKit}">Open AppSec Kit</button>
            </div>
        `;
    }

    openAppSecKit() {
        window.open(this.appSecRoute, "_blank");
    }

    handleMessage(message: ServerMessage): boolean {
        if (message.command === "appsec-kit-init") {
            this.appSecRoute = "/" + message.data.appSecRoute;
            this.message = "AppSec Kit is configured and scanning app dependencies for known vulnerabilities."
            return true;
        } else if (message.command === "appsec-kit-scan") {
            if (message.data.vulnerabilityCount > 0) {
                this.message = message.data.vulnerabilityCount + " potential vulnerabilities found.";
            } else {
                this.message = "No vulnerabilities found."
            }
            return true;
        } else {
            return false; // not a plugin command
        }
    }
}

const panelConfig: PanelConfiguration = {
    header: 'AppSec Kit',
    expanded: false,
    panelOrder: 65,
    panel: 'right',
    floating: false,
    tag: 'appsec-kit-plugin',
};

const copilotPlugin: CopilotPlugin = {
    init(copilotInterface: CopilotInterface): void {
        copilotInterface.addPanel(panelConfig);
    },
};
(window as any).Vaadin.copilot.plugins.push(copilotPlugin);
