// ConnectionServiceAIDL.aidl
package com.safe_bicycle_assistant.s_ba;

// Declare any non-default types here with import statements

interface ConnectionServiceAIDL {

    void testConnection();
    void destroyService();
    void reverseConnection(in Bundle bundle);
}
