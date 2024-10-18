/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.extension

import org.pf4j.spring.ExtensionsInjector
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory

class ValtimoExtensionsInjector(
    private val extensionManager: ExtensionManager,
    beanFactory: AbstractAutowireCapableBeanFactory
) : ExtensionsInjector(extensionManager, beanFactory) {

    public override fun registerExtension(extensionClass: Class<*>) {
        super.registerExtension(extensionClass)

        val bean = beanFactory.getBean(extensionClass)
        extensionManager.extensionRegistrationListeners.forEach { listener ->
            listener.extensionRegistered(extensionClass, bean)
        }
    }

    fun unregisterExtension(extensionClassName: String) {
        beanFactory.destroySingleton(extensionClassName)
    }
}