//   Copyright 2011 Anaplan Inc.
//
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client;

import com.anaplan.client.api.AnaplanAPI;

/**
 * Base class for remote server object accessor classes. No publicly accessible methods are introduced by this class; it
 * contains only members relevant to the implementation
 */
public abstract class AnaplanApiClientObject {

  private final Service service;

  AnaplanApiClientObject(Service service) {
    this.service = service;
  }

  public Service getService() {
    return service;
  }

  public AnaplanAPI getApi() {
    return service.getApiProvider().get();
  }
}
