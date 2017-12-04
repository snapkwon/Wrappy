package net.wrappy.im.GethService.contracts;


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

import java.math.BigInteger;


public class PRDToken {
    // ABI is the input ABI used to generate the binding from.
    //[{"constant":true,"inputs":[{"name":"","type":"address"}],"name":"balanceOf","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_to","type":"address"},{"name":"_value","type":"uint256"}],"name":"transfer","outputs":[],"payable":false,"type":"function"},{"inputs":[{"name":"initialSupply","type":"uint256"}],"payable":false,"type":"constructor"}]
    public final static String ABI = "[{\"constant\":true,\"inputs\":[\n" +
            "{\"name\":\"\",\"type\":\"address\"}\n" +
            "],\"name\":\"balanceOf\",\"outputs\":[\n" +
            "{\"name\":\"\",\"type\":\"uint256\"}\n" +
            "],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[\n" +
            "{\"name\":\"_to\",\"type\":\"address\"}\n" +
            ",\n" +
            "{\"name\":\"_value\",\"type\":\"uint256\"}\n" +
            "],\"name\":\"transfer\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"inputs\":[\n" +
            "{\"name\":\"initialSupply\",\"type\":\"uint256\"}\n" +
            "],\"payable\":false,\"type\":\"constructor\"}]";
    public static final String contractAddress =  "0xa31cbC0d8CB64542518a6950aa9a74CcfCf52Ba2";
    public static final long BLOCK =  757486;

    // Contract instance bound to a blockchain address.
    private final BoundContract Contract;

    public static final String fnTransfer = "0xa9059cbb000000000000000000000000";

    // Creates a new instance of Main, bound to a specific deployed contract.
    public PRDToken(EthereumClient client) throws Exception {
        this.Contract = Geth.bindContract(Geth.newAddressFromHex(contractAddress), ABI, client);
    }

    // balanceOf is a free data retrieval call binding the contract method 0x70a08231.
    //
    // Solidity: function balanceOf( address) constant returns(uint64)
    public BigInt balanceOf(CallOpts opts, Address accAddress){
        try {
            Interfaces args = Geth.newInterfaces(1);
            Interface arg = Geth.newInterface();
            arg.setDefaultAddress();
            arg.setAddress(accAddress);
            args.set(0, arg);


            Interfaces results = Geth.newInterfaces(1);
            Interface result0 = Geth.newInterface();
            result0.setDefaultBigInt();
            results.set(0, result0);

            if (opts == null) {
                opts = Geth.newCallOpts();
            }

            this.Contract.call(opts, results, "balanceOf", args);
            BigInt bigInt = results.get(0).getBigInt();
            return bigInt;
        }catch (Exception e) {
            e.getLocalizedMessage();
            return new BigInt(0);
        }
    }

    // transfer is a paid mutator transaction binding the contract method 0x5d359fbd.
    //
    // Solidity: function transfer(_to address, _value uint64) returns()
    public Transaction transfer(TransactOpts opts, Address _to, BigInt _value) throws Exception {
        Interfaces args = Geth.newInterfaces(2);

        Interface arg = Geth.newInterface();
        arg.setDefaultAddress();
        arg.setAddress(_to);
        args.set(0, arg);

        Interface arg1 = Geth.newInterface();
        arg1.setDefaultBigInt();
        arg1.setBigInt(_value);
        args.set(1, arg1);

        return this.Contract.transact(opts, "transfer"	, args);

    }

    public static TransferResponse getInputData(String inputData) throws Exception{
        TransferResponse response = new TransferResponse();
        response.to = Geth.newAddressFromHex("0x0000000000000000000000000000000000000000");
        response.value = new BigInt(0);
        try{
            if (inputData.length() > 0 && inputData.contains(fnTransfer)){
                String tmp = inputData.replace(fnTransfer, "");
                response.to = Geth.newAddressFromHex("0x" + tmp.substring(0,40));
                BigInteger bigInt = new BigInteger(tmp.substring(41), 16);
                response.value = new BigInt(bigInt.longValue());
            }

        }catch (Exception e) {
            e.getLocalizedMessage();
        }
        return response;
    }

    public static class TransferResponse {
        public Address to;
        public BigInt value;
    }


}

