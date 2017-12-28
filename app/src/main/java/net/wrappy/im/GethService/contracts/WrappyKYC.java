
// This file is an automatically generated Java binding. Do not modify as any
// change will likely be lost upon the next re-generation!

package net.wrappy.im.GethService.contracts;

import net.wrappy.im.util.BundleKeyConstant;

import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.BoundContract;
import org.ethereum.geth.CallOpts;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Interface;
import org.ethereum.geth.Interfaces;
import org.ethereum.geth.TransactOpts;
import org.ethereum.geth.Transaction;


	public class WrappyKYC {
		// ABI is the input ABI used to generate the binding from.
		public final static String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"phone\",\"type\":\"uint256\"}],\"name\":\"getAddressByPhone\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"phone\",\"type\":\"uint256\"}],\"name\":\"newUser\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"addressPhonePair\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"},{\"name\":\"phone\",\"type\":\"uint256\"}],\"name\":\"newUserByWrappy\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"getPhoneByAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"phoneAddressPair\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"newWrappyAccount\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\"}]";
		public final static String contractAddress =  "0xf19e1B5a2722748348E69A9c6089ca2ffcb191e3";


			// BYTECODE is the compiled bytecode used for deploying new contracts.
			public final static byte[] BYTECODE = "6060604052341561000f57600080fd5b5b336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5b6107c2806100616000396000f3006060604052361561008c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063036796661461009157806358e25678146100f457806371d01c73146101175780638da5cb5b14610164578063947fa82e146101b9578063b958a5e1146101fb578063ceaecc8414610248578063f206e912146102ab575b600080fd5b341561009c57600080fd5b6100b260048080359060200190919050506102e4565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156100ff57600080fd5b6101156004808035906020019091905050610322565b005b341561012257600080fd5b61014e600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061041c565b6040518082815260200191505060405180910390f35b341561016f57600080fd5b610177610434565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156101c457600080fd5b6101f9600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610459565b005b341561020657600080fd5b610232600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610602565b6040518082815260200191505060405180910390f35b341561025357600080fd5b610269600480803590602001909190505061064c565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156102b657600080fd5b6102e2600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061067f565b005b60006003600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690505b919050565b60011515600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff1615151415151561038257600080fd5b336003600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055505b50565b60026020528060005260406000206000915090505481565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b3373ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141515156104b557600080fd5b60011515600160008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff1615151415156105675760018060008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055505b816003600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600260008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055505b5050565b6000600260008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205490505b919050565b60036020528060005260406000206000915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b3373ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141515156106db57600080fd5b60001515600160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff1615151415151561073b57600080fd5b60018060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055505b505600a165627a7a723058204e7d3579397df6f2775a8f4ffe10eaf1cc351c7699ea5c53494e3872250b29130029".getBytes();

			// deploy deploys a new Ethereum cont
			// ract, binding an instance of WrappyKYC to it.
			public static WrappyKYC deploy(TransactOpts auth, EthereumClient client) throws Exception {
				Interfaces args = Geth.newInterfaces(0);
				
				return new WrappyKYC(Geth.deployContract(auth, ABI, BYTECODE, client, args));
			}

			// Internal constructor used by contract deployment.
			private WrappyKYC(BoundContract deployment) {
				this.Address  = deployment.getAddress();
				this.Deployer = deployment.getDeployer();
				this.Contract = deployment;
			}
		

		// Ethereum address where this contract is located at.
		public final Address Address;

		// Ethereum transaction in which this contract was deployed (if known!).
		public final Transaction Deployer;

		// Contract instance bound to a blockchain address.
		private final BoundContract Contract;

		// Creates a new instance of WrappyKYC, bound to a specific deployed contract.
		public WrappyKYC(EthereumClient client) throws Exception {
			this(Geth.bindContract(Geth.newAddressFromHex(contractAddress), ABI, client));
		}

		
			

			// addressPhonePair is a free data retrieval call binding the contract method 0x71d01c73.
			//
			// Solidity: function addressPhonePair( address) constant returns(uint256)
			public BigInt addressPhonePair(CallOpts opts, Address arg0) throws Exception {
				Interfaces args = Geth.newInterfaces(1);
				args.set(0, Geth.newInterface()); args.get(0).setAddress(arg0);
				

				Interfaces results = Geth.newInterfaces(1);
				Interface result0 = Geth.newInterface();
				result0.setDefaultBigInt();
				results.set(0, result0);
				

				if (opts == null) {
					opts = Geth.newCallOpts();
				}
				this.Contract.call(opts, results, "addressPhonePair", args);
				return results.get(0).getBigInt();
				
			}
		
			

			// getAddressByPhone is a free data retrieval call binding the contract method 0x03679666.
			//
			// Solidity: function getAddressByPhone(phone uint256) constant returns(address)
			public Address getAddressByPhone(CallOpts opts, BigInt phone) throws Exception {
				Interfaces args = Geth.newInterfaces(1);
				Interface arg = Geth.newInterface();
				arg.setDefaultBigInt();
				arg.setBigInt(phone);
				args.set(0,arg);

				Interfaces results = Geth.newInterfaces(1);
				Interface result0 = Geth.newInterface();
				result0.setDefaultAddress();
				results.set(0, result0);
				

				if (opts == null) {
					opts = Geth.newCallOpts();
				}
				this.Contract.call(opts, results, "getAddressByPhone", args);
				return results.get(0).getAddress();
				
			}
		
			

			// getPhoneByAddress is a free data retrieval call binding the contract method 0xb958a5e1.
			//
			// Solidity: function getPhoneByAddress(addr address) constant returns(uint256)
			public BigInt getPhoneByAddress(CallOpts opts, Address addr) throws Exception {
                Interfaces args = Geth.newInterfaces(1);
                Interface arg = Geth.newInterface();
                arg.setDefaultAddress();
                arg.setAddress(addr);
                args.set(0, arg);


                Interfaces results = Geth.newInterfaces(1);
                Interface result0 = Geth.newInterface();
                result0.setDefaultBigInt();
                results.set(0, result0);
				

				if (opts == null) {
					opts = Geth.newCallOpts();
				}
				this.Contract.call(opts, results, "getPhoneByAddress", args);
				return results.get(0).getBigInt();
				
			}
		
			

			// owner is a free data retrieval call binding the contract method 0x8da5cb5b.
			//
			// Solidity: function owner() constant returns(address)
			public Address owner(CallOpts opts) throws Exception {
				Interfaces args = Geth.newInterfaces(0);
				

				Interfaces results = Geth.newInterfaces(1);
				Interface result0 = Geth.newInterface(); result0.setDefaultAddress(); results.set(0, result0);
				

				if (opts == null) {
					opts = Geth.newCallOpts();
				}
				this.Contract.call(opts, results, "owner", args);
				return results.get(0).getAddress();
				
			}
		
			

			// phoneAddressPair is a free data retrieval call binding the contract method 0xceaecc84.
			//
			// Solidity: function phoneAddressPair( uint256) constant returns(address)
			public Address phoneAddressPair(CallOpts opts, BigInt arg0) throws Exception {
				Interfaces args = Geth.newInterfaces(1);
				args.set(0, Geth.newInterface()); args.get(0).setBigInt(arg0);
				

				Interfaces results = Geth.newInterfaces(1);
				Interface result0 = Geth.newInterface(); result0.setDefaultAddress(); results.set(0, result0);
				

				if (opts == null) {
					opts = Geth.newCallOpts();
				}
				this.Contract.call(opts, results, "phoneAddressPair", args);
				return results.get(0).getAddress();
				
			}
		

		
			// newUser is a paid mutator transaction binding the contract method 0x58e25678.
			//
			// Solidity: function newUser(phone uint256) returns()
			public Transaction newUser(TransactOpts opts, BigInt phone) throws Exception {
				Interfaces args = Geth.newInterfaces(1);
				args.set(0, Geth.newInterface()); args.get(0).setBigInt(phone);
				

				return this.Contract.transact(opts, BundleKeyConstant.NEW_USER_KEY	, args);
			}
		
			// newWrappyAccount is a paid mutator transaction binding the contract method 0xf206e912.
			//
			// Solidity: function newWrappyAccount(addr address) returns()
			public Transaction newWrappyAccount(TransactOpts opts, Address addr) throws Exception {
				Interfaces args = Geth.newInterfaces(1);
				args.set(0, Geth.newInterface()); args.get(0).setAddress(addr);
				

				return this.Contract.transact(opts, "newWrappyAccount"	, args);
			}

            public Transaction newUserByWrappy(TransactOpts opts, Address addr, BigInt phone) throws Exception {
                Interfaces args = Geth.newInterfaces(2);
            //    args.set(0, Geth.newInterface()); args.get(0).setAddress(addr);
               // args.set(1, Geth.newInterface()); args.get(1).setBigInt(phone);

				Interface arg = Geth.newInterface();
				arg.setDefaultAddress();
				arg.setAddress(addr);
				args.set(0, arg);

				Interface arg1 = Geth.newInterface();
				arg1.setDefaultBigInt();
				arg1.setBigInt(phone);
				args.set(1, arg1);


                return this.Contract.transact(opts, "newUserByWrappy", args);
            }
		
	}

