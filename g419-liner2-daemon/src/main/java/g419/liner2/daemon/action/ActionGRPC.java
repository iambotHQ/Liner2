package g419.liner2.daemon.action;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.daemon.utils.GrpcWorker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class ActionGRPC extends Action {

    public static final String OPTION_HOSTNAME = "H";
    public static final String OPTION_HOSTNAME_LONG = "hostname";
    public static final String OPTION_HOSTNAME_DEFAULT = "localhost";
    public static final String OPTION_HOSTNAME_ARG = "name";
    public static final String OPTION_HOSTNAME_DESC = "gRPC hostname";

    public static final String OPTION_PORT = "P";
    public static final String OPTION_PORT_LONG = "port";
    public static final String OPTION_PORT_DEFAULT = "5010";
    public static final String OPTION_PORT_ARG = "name";
    public static final String OPTION_PORT_DESC = "gRPC port";

    private String modelPath;
    private String grpcHostname;
    private String grpcPort;

    public ActionGRPC() {
        super("grpc");
        setDescription("Starts Liner2 daemon processing requests via gRPC");
        options.addOption(getHostnameOption());
        options.addOption(getPortOption());
        options.addOption(CommonOptions.getModelFileOption());
        options.addOption(CommonOptions.getInputFileFormatOption());
    }

    private static Option getHostnameOption() {
        return Option.builder(OPTION_HOSTNAME).longOpt(OPTION_HOSTNAME_LONG)
                .hasArg().argName(OPTION_HOSTNAME_ARG).desc(OPTION_HOSTNAME_DESC).build();
    }

    private static Option getPortOption() {
        return Option.builder(OPTION_PORT).longOpt(OPTION_PORT_LONG)
                .hasArg().argName(OPTION_PORT_ARG).desc(OPTION_PORT_DESC).build();
    }


    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        modelPath = line.getOptionValue(CommonOptions.OPTION_MODEL);
        grpcHostname = line.getOptionValue(OPTION_HOSTNAME, OPTION_HOSTNAME_DEFAULT);
        grpcPort = line.getOptionValue(OPTION_PORT, OPTION_PORT_DEFAULT);
    }

    @Override
    public void run() throws Exception {
        final GrpcWorker worker = new GrpcWorker(grpcHostname, Integer.parseInt(grpcPort), modelPath);
        worker.run();
    }
}