package main

import (
    "fmt"
    "crypto/x509"
    "github.com/sandertv/gophertunnel/minecraft/auth"
    "github.com/sandertv/gophertunnel/minecraft/protocol/packet"
    "github.com/sandertv/gophertunnel/minecraft/protocol/login"
    "bytes"
    rand2 "math/rand"
    "time"
    "github.com/sandertv/gophertunnel/minecraft/protocol"
    "github.com/google/uuid"
    "encoding/base64"
    "C"
    "unsafe"
    "crypto/ecdsa"
    "encoding/pem"
    _ "reflect"
)

func parseKey(pem_private_key []byte) (*ecdsa.PrivateKey, error) {
    p, _ := pem.Decode(pem_private_key)
    if p == nil {
        return nil, fmt.Errorf("Error parsing key: nil after decode")
    }
    key, e := x509.ParsePKCS8PrivateKey(p.Bytes)
    if e != nil {
        return nil, fmt.Errorf("Error parsing key: %v", e)
    }
    ecdsa_key := key.(*ecdsa.PrivateKey)
    return ecdsa_key, nil
}

//export AuthChain
func AuthChain(email string, password string, private_key []byte) (auth_chain *C.char, err *C.char) {
    key, e := parseKey(private_key)
    if e != nil {
        return nil, C.CString(fmt.Errorf("Error parsing key: %v", e).Error());
    }
    auth_chain_str, e := authChain(email, password, key)
    if e != nil {
        return nil, C.CString(e.Error())
    }
    return C.CString(auth_chain_str), nil
}

// authChain requests the Minecraft auth JWT chain using the credentials passed. If successful, an encoded
// chain ready to be put in a login request is returned.
func authChain(email, password string, key *ecdsa.PrivateKey) (string, error) {
	// Obtain the Live token, and using that the XSTS token.
	liveToken, err := auth.RequestLiveToken(email, password)
	if err != nil {
		return "", fmt.Errorf("error obtaining Live token: %v", err)
	}
	xsts, err := auth.RequestXSTSTokenUserOnly(liveToken)
	if err != nil {
		return "", fmt.Errorf("error obtaining XSTS token: %v", err)
	}

	// Obtain the raw chain data using the
	chain, err := auth.RequestMinecraftChain(xsts, key)
	if err != nil {
		return "", fmt.Errorf("error obtaining Minecraft auth chain: %v", err)
	}
	return chain, nil
}

//export EncodeLoginPacket
func EncodeLoginPacket(chainData string, address string, private_key []byte, current_protocol int32, current_version string) (login_packet_bytes unsafe.Pointer, login_packet_bytes_length int, xuid *C.char, identity *C.char, e *C.char) {
    key, err := parseKey(private_key)
    if err != nil {
        return nil, 0, nil, nil, C.CString(fmt.Errorf("Error parsing key: %v", err).Error());
    }
    clientData := defaultClientData(address, current_version)
    request := login.Encode(chainData, clientData, key)

    // Probably need this, right?
    identityData, _, _ := login.Decode(request)

    loginPacket := &packet.Login{ConnectionRequest: request, ClientProtocol: current_protocol}

    buf := bytes.NewBuffer(nil)
    loginPacket.Marshal(buf)
    return C.CBytes(buf.Bytes()), len(buf.Bytes()), C.CString(identityData.XUID), C.CString(identityData.Identity), nil
}



// defaultClientData returns a valid, mostly filled out ClientData struct using the connection address
// passed, which is sent by default, if no other client data is set.
func defaultClientData(address string, current_version string) login.ClientData {
	rand2.Seed(time.Now().Unix())
	return login.ClientData{
		ClientRandomID:  rand2.Int63(),
		DeviceOS:        protocol.DeviceWin10,
		GameVersion:     current_version,
		DeviceID:        uuid.Must(uuid.NewRandom()).String(),
		LanguageCode:    "en_GB",
		ThirdPartyName:  "Steve",
		SelfSignedID:    uuid.Must(uuid.NewRandom()).String(),
		ServerAddress:   address,
		SkinID:          uuid.Must(uuid.NewRandom()).String(),
		SkinData:        base64.StdEncoding.EncodeToString(bytes.Repeat([]byte{0, 0, 0, 255}, 32*64)),
		SkinImageWidth:  64,
		SkinImageHeight: 32,
	}
}


func main() {}