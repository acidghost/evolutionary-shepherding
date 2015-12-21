import pandas as pd


# stats_path = "/Users/erotundo/git/UniNotes/ASO/statistics"
stats_path = "../statistics"

runs = ['hetero.1v1',
        'hetero.2v1',
        'hetero.3v1',
        'hetero.3v2.ho',
        'hetero.3v3.ho']

# int generation;
# List<SubpopData> subpopData;
# int corralled;
# int escaped;
# int evaluations;
# double corralledRatio;
# double escapedRatio;
# double popMeanFitness;
# double popBestOfGeneration;
# double popBestSoFar;

# double mean;
# double bestOfGeneration;
# double bestSoFar;

fitness_cols = {
    1: [2],
    2: [2, 5],
    3: [2, 5, 8]
}

for run in runs:
    num_sub_pop = int(run.split('.')[1].split('v')[0])
    print run
    df = pd.DataFrame()
    for i in range(1, 41):
        file_path = stats_path + str("/" + str(run) + "/" + str(i)+'.stat')     
        df_run = pd.read_csv(file_path, sep=' ', header=None, index_col=0, skipfooter=1, engine='python')
        dogs_fitness = df_run.iloc[:, fitness_cols[num_sub_pop]].mean(axis=1)
        mean_dog_fitness = dogs_fitness.mean()
        # print mean_dog_fitness
        mean_corralled_ratio = df_run.iloc[:, -5].mean()
        # print mean_corralled_ratio
        
        df.loc[i, "fitness"] = mean_dog_fitness
        df.loc[i, "corralled_ratio"] = mean_corralled_ratio

    # print df
    print df.loc[:, "fitness"].describe()
    print df.loc[:, "corralled_ratio"].describe()
    print ""
