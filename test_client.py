import socket
import time

def test_flow():
    # 1. Connect to Messaging Service (Netty)
    print("Connecting to Messaging Service (Netty)...")
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(('localhost', 5222))
    
    # 2. Register user
    print("Registering user 'deniz'...")
    s.sendall(b"ID:deniz\n")
    data = s.recv(1024)
    print(f"Received: {data.decode().strip()}")
    
    print("\nNow waiting for messages... (Send a gRPC request to test)")
    try:
        while True:
            data = s.recv(1024)
            if data:
                print(f"!!! MESSAGE RECEIVED: {data.decode().strip()} !!!")
    except KeyboardInterrupt:
        s.close()

if __name__ == "__main__":
    test_flow()
